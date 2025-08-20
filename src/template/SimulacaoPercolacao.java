package template;

import aesd.ds.implementations.nonlinear.uf.QuickFindUF;
import aesd.ds.implementations.nonlinear.uf.QuickUnionUF;
import aesd.ds.implementations.nonlinear.uf.UF;
import aesd.ds.implementations.nonlinear.uf.WeightedQuickUnionPathCompressionUF;
import aesd.ds.implementations.nonlinear.uf.WeightedQuickUnionUF;
import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.math.MathUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Simulador de Percolação usando JSGE.
 * 
 * @author Prof. Dr. David Buzatto
 */
public class SimulacaoPercolacao extends EngineFrame {
    
    private record Posicao( int linha, int coluna ) {
    }
    
    private int tamanho;
    private boolean[][] celulas;
    
    private Posicao[] posicoes;
    private int quantidadeMarcados;
    
    private double tempoParaMarcar;
    private double contadorTempo;
    
    private UF uf;
    private int meioCima;
    private int meioBaixo;
    
    private boolean executarTudo = true;
    private boolean pronto = false;
    private long tempoTotal;
    
    public SimulacaoPercolacao() {
        super( 800, 800, "Percolação", 60, true );
    }
    
    @Override
    public void create() {
        
        MathUtils.setRandomSeed( 0 );
        setDefaultFontSize( 20 );
        
        tamanho = 800;
        celulas = new boolean[tamanho][tamanho];
        posicoes = new Posicao[tamanho*tamanho];
        
        for ( int i = 0; i < tamanho; i++ ) {
            for ( int j = 0; j < tamanho; j++ ) {
                posicoes[i * tamanho + j] = new Posicao( i, j );
            }
        }
        embaralhar( posicoes );
        
        tempoParaMarcar = 0.005;
        
        uf = new WeightedQuickUnionUF( posicoes.length + 2 );
        //uf = new WeightedQuickUnionUF( posicoes.length + 2 );
        //uf = new WeightedQuickUnionUF( posicoes.length + 2 );
        meioCima = posicoes.length;
        meioBaixo = posicoes.length+1;
        
    }
    
    @Override
    public void update( double delta ) {
        
        if ( !pronto && executarTudo ) {
            
            long tempoAntes = System.currentTimeMillis();
            
            while ( !uf.connected( meioCima, meioBaixo ) ) {
                Posicao p = posicoes[quantidadeMarcados++];
                celulas[p.linha][p.coluna] = true;
                unirVizinhos( p );
            }
            
            tempoTotal = System.currentTimeMillis() - tempoAntes;
            pronto = true;
            
        } else {
            
            contadorTempo += delta;

            if ( quantidadeMarcados < posicoes.length && !uf.connected( meioCima, meioBaixo ) ) {
                if ( contadorTempo > tempoParaMarcar ) {
                    contadorTempo = 0;
                    Posicao p = posicoes[quantidadeMarcados++];
                    celulas[p.linha][p.coluna] = true;
                    unirVizinhos( p );
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
                if ( celulas[i][j] ) {
                    fillRectangle( j * largura, i * largura, largura, largura, WHITE );
                }
            }
        }
        
        int id = uf.find( meioCima );
        for ( int i = 0; i < tamanho * tamanho; i++ ) {
            if ( uf.find( i ) == id ) {
                Posicao pos = mapear1D2d( i );
                fillRectangle( pos.coluna * largura, pos.linha * largura, largura, largura, BLUE );
            }
        }
        
        if ( tamanho < getScreenWidth() / 2 ) {
            for ( int i = 0; i <= tamanho; i++ ) {
                drawLine( 0, largura * i, getScreenWidth(), largura * i, BLACK );
                drawLine( largura * i, 0, largura * i, getScreenHeight(), BLACK );
            }
        }
        
        fillRoundRectangle( 10, 10, 300, 30, 10, BLACK );
        drawText( String.format( "%d/%d (%.2f%%)", quantidadeMarcados, tamanho * tamanho, (double) quantidadeMarcados / ( tamanho * tamanho ) * 100 ), 20, 20, WHITE );
        
        if ( executarTudo ) {
            fillRoundRectangle( 10, 50, 300, 30, 10, BLACK );
            drawText( String.format( "tempo total: %d", tempoTotal ), 20, 60, WHITE );
        }
    
    }
    
    private void embaralhar( Posicao[] array ) {
        for ( int i = 0; i < array.length; i++ ) {
            int p = MathUtils.getRandomValue( 0, array.length - 1 );
            Posicao t = array[i];
            array[i] = array[p];
            array[p] = t;
        }
    }
    
    private void unirVizinhos( Posicao p ) {
        
        int linha = p.linha;
        int coluna = p.coluna;
        
        // cima
        if ( deveUnir( linha-1, coluna ) ) {
            uf.union(
                mapear2D1D( linha, coluna ),
                mapear2D1D( linha-1, coluna )
            );
        }
        
        // baixo
        if ( deveUnir( linha+1, coluna ) ) {
            uf.union(
                mapear2D1D( linha, coluna ),
                mapear2D1D( linha+1, coluna )
            );
        }
        
        // esquerda
        if ( deveUnir( linha, coluna-1 ) ) {
            uf.union(
                mapear2D1D( linha, coluna ),
                mapear2D1D( linha, coluna-1 )
            );
        }
        
        // direita
        if ( deveUnir( linha, coluna+1 ) ) {
            uf.union(
                mapear2D1D( linha, coluna ),
                mapear2D1D( linha, coluna+1 )
            );
        }
        
        // casos especiais:
        // primeira linha
        if ( linha == 0 ) {
            uf.union(
                mapear2D1D( linha, coluna ),
                meioCima
            );
        }
        
        // última linha
        if ( linha == tamanho - 1 ) {
            uf.union(
                mapear2D1D( linha, coluna ),
                meioBaixo
            );
        }
        
    }
    
    private boolean existePosicao( int linha, int coluna ) {
        return linha >= 0 && linha < tamanho && coluna >= 0 && coluna < tamanho;
    }
    
    private boolean deveUnir( int linha, int coluna ) {
        return existePosicao( linha, coluna ) && celulas[linha][coluna];
    }
    
    private int mapear2D1D( int linha, int coluna ) {
        return linha * tamanho + coluna;
    }
    
    private Posicao mapear1D2d( int posicao ) {
        return new Posicao( posicao / tamanho, posicao % tamanho );
    }
    
    public static void main( String[] args ) {
        new SimulacaoPercolacao();
    }
    
}
