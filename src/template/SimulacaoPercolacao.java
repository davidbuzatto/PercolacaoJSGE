package template;

import aesd.ds.implementations.nonlinear.uf.UF;
import aesd.ds.implementations.nonlinear.uf.WeightedQuickUnionUF;
import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.core.utils.ColorUtils;
import br.com.davidbuzatto.jsge.math.MathUtils;
import java.awt.Color;

/**
 * Simulador de Percolação usando JSGE.
 * 
 * Material para a disciplina de Estruturas de Dados do curso de Bacharelado
 * em Ciência da Computação.
 * 
 * @author Prof. Dr. David Buzatto
 */
public class SimulacaoPercolacao extends EngineFrame {
    
    public record Posicao( int linha, int coluna ){
    }
    
    private int tamanho;
    private boolean[][] celulas;
    private int quantidade;
    private int[] posicoes;
            
    private double tempoPerfuracao;
    private double contadorTempo;
    
    private UF uf;
    private int pCima;
    private int pBaixo;
    
    private boolean destacarConjuntos;
    private boolean fazerTudo;
    private boolean pronto;
    
    private long tempoTotal;
    private Color corFundoEstatisticas;
    
    public SimulacaoPercolacao() {
        super( 800, 800, "Percolação", 60, true);
    }
    
    @Override
    public void create() {
        
        MathUtils.setRandomSeed( 0 );
        setDefaultFontSize( 20 );
        
        tamanho = 400;
        celulas = new boolean[tamanho][tamanho];
        posicoes = new int[tamanho*tamanho];
        
        for ( int i = 0; i < posicoes.length; i++ ) {
            posicoes[i] = i;
        }
        embaralhar( posicoes );
        
        tempoPerfuracao = 0.016;  // 0.016 é o mínimo para 60 FPS
        
        //uf = new QuickFindUF( tamanho * tamanho + 2 );
        //uf = new QuickUnionUF( tamanho * tamanho + 2 );
        uf = new WeightedQuickUnionUF( tamanho * tamanho + 2 );
        //uf = new WeightedQuickUnionPathCompressionUF( tamanho * tamanho + 2 );
        
        pCima = tamanho * tamanho;
        pBaixo = pCima + 1;
        
        destacarConjuntos = false;
        fazerTudo = true;
        
        corFundoEstatisticas = ColorUtils.fade( BLACK, 0.5 );
        
    }

    @Override
    public void update( double delta ) {
        
        if ( fazerTudo ) {
            
            if ( !pronto ) {
                
                long tempoAntes = System.currentTimeMillis();

                while ( !uf.connected( pCima, pBaixo ) ) {
                    Posicao p = mapear1D2D( posicoes[quantidade] );
                    celulas[p.linha][p.coluna] = true;
                    conectarVizinhos( p );
                    quantidade++;
                }

                pronto = true;
                tempoTotal = System.currentTimeMillis() - tempoAntes;
                
            }
            
        } else {
            
            contadorTempo += delta;

            if ( !uf.connected( pCima, pBaixo ) ) {
                if ( quantidade < tamanho * tamanho && contadorTempo >= tempoPerfuracao ) {
                    contadorTempo = 0;
                    Posicao p = mapear1D2D( posicoes[quantidade] );
                    celulas[p.linha][p.coluna] = true;
                    conectarVizinhos( p );
                    quantidade++;
                }
            }
            
        }
        
    }
    
    @Override
    public void draw() {
        
        clearBackground( GRAY );
        
        int largura = getScreenWidth() / tamanho;
        
        for ( int i = 0; i < tamanho; i++ ) {
            for ( int j = 0; j < tamanho; j++ ) {
                
                Color c = WHITE;
                int p = mapear2D1D( i, j );
                int id = uf.find( p );
                
                if ( destacarConjuntos ) {
                    // pintura das cores dos componentes conectados
                    double porc = ( (double) id / ( tamanho * tamanho ) );
                    double angulo = porc * 360;
                    c = ColorUtils.colorFromHSV( angulo, 1, 1 );
                } else {
                    // pinta de azul apenas os sítios conectados ao pCima
                    int idCima = uf.find( pCima );
                    if ( id == idCima ) {
                        c = BLUE;
                    }
                }
                
                if ( celulas[i][j] ) {
                    fillRectangle( 
                        j * largura, 
                        i * largura, 
                        largura,
                        largura,
                        c
                    );
                }
                
            }
        }
        
        if ( tamanho < 400 ) {
            for ( int i = 0; i <= tamanho; i++ ) {
                drawLine( 0, largura * i, getScreenWidth(), largura * i, BLACK );
                drawLine( largura * i, 0, largura * i, getScreenHeight(), BLACK );
            }
        }
        
        fillRoundRectangle( 10, 10, 350, 25, 10, corFundoEstatisticas );
        drawText( String.format( "%d/%d (%.2f%%)", quantidade, tamanho * tamanho, (double) quantidade / ( tamanho * tamanho ) * 100 ), 15, 15, WHITE );
        
        if ( fazerTudo ) {
            fillRoundRectangle( 10, 40, 350, 25, 10, corFundoEstatisticas );
            drawText( String.format( "tempo total: %d", tempoTotal ), 15, 45, WHITE );
        }
    
    }
    
    private Posicao mapear1D2D( int p ) {
        return new Posicao( p / tamanho, p % tamanho );
    }
    
    private int mapear2D1D( Posicao p ) {
        return p.linha * tamanho + p.coluna;
    }
    
    private int mapear2D1D( int linha, int coluna ) {
        return linha * tamanho + coluna;
    }
    
    private void embaralhar( int[] array ) {
        for ( int i = 0; i < array.length; i++ ) {
            int p = MathUtils.getRandomValue( 0, array.length - 1 );
            int t = array[i];
            array[i] = array[p];
            array[p] = t;
        }
    }
    
    private void conectarVizinhos( Posicao p ) {
        
        int linha = p.linha;
        int coluna = p.coluna;
        int pAtual = mapear2D1D( p );
        
        // cima
        if ( existeCelula( linha-1, coluna ) && celulas[linha-1][coluna] ) {
            int pVizinho = mapear2D1D( linha-1, coluna );
            uf.union( pAtual, pVizinho );
        }
        
        // baixo
        if ( existeCelula( linha+1, coluna ) && celulas[linha+1][coluna] ) {
            int pVizinho = mapear2D1D( linha+1, coluna );
            uf.union( pAtual, pVizinho );
        }
        
        // esquerda
        if ( existeCelula( linha, coluna-1 ) && celulas[linha][coluna-1] ) {
            int pVizinho = mapear2D1D( linha, coluna-1 );
            uf.union( pAtual, pVizinho );
        }
        
        // direita
        if ( existeCelula( linha, coluna+1 ) && celulas[linha][coluna+1] ) {
            int pVizinho = mapear2D1D( linha, coluna+1 );
            uf.union( pAtual, pVizinho );
        }
        
        // linha de cima
        if ( linha == 0 ) {
            uf.union( pAtual, pCima );
        }
        
        // linha de baixo
        if ( linha == tamanho - 1 ) {
            uf.union( pAtual, pBaixo );
        }
        
    }
    
    private boolean existeCelula( int linha, int coluna ) {
        return linha >= 0 && linha < tamanho && coluna >= 0 && coluna < tamanho;
    }
    
    public static void main( String[] args ) {
        new SimulacaoPercolacao();
    }
    
}
