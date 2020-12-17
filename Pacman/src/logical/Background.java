package logical;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Background extends JPanel implements ActionListener{

	private Dimension d;
	private final Font smallfont = new Font("Arial", Font.BOLD, 14);
	private boolean inGame = false;
	private boolean dying = false;
	
	private final int BLOCK_SIZE = 24; //Que tan grande es un bloquecito
	private final int N_BLOCKS = 15; //Cantidad de bloquecitos tanto de alto x ancho. 15*15 = 255 posiciones
	private final int SCREEN_SIZE = N_BLOCKS * BLOCK_SIZE; //Tamaño real de todo el fondo
	private final int MAX_GHOSTS = 4;
	private int N_GHOSTS; 
	private final int PACMAN_SPEED = 3; //Velocidad de pacman
	
	
	private int lives, score;
	private int [] dx, dy; //Determina direcciones
	private int [] ghost_x, ghost_y, ghost_dx, ghost_dy, ghostSpeed;
	
	//Sprites de las vidas, fantasmas y distintas posiciones de pacman
	private Image heart, ghost;
	private Image up, down, left, right;
	
	
	private int pacman_x, pacman_y, pacmand_x, pacmand_y; //Posicion y dirección de Pacman. (Por eso la "d" al final)
	private int req_dx, req_dy; //Estas dos variables están determinadas en la clase TAdapter. Keyboards key
	
	private final int validSpeeds[] = {1, 2, 3, 4, 6, 8};
	private final int maxSpeed = 6;
	private int currentSpeed;
	private short [] screenData; //Obtiene toda la información de levelData. De esta forma podemos "repintar" el nivel
	private Timer timer; //Necesario para pintar las animaciones
	
	/*Acá se imprime el diseño del nivel
	 * 										Guía de diseño del nivel:
	 * 0 = obstáculo		4 = borde DERECHO
	 * 1 = borde IZQUIERDO	8 = borde INFERIOR
	 * 2 = borde SUPERIOR	16 = comida pacman
	 * 
	 * Para diseñar un unico bloquecito hacemos la suma de todos los elementos que el bloque tendra adentro.
	 * Por ejemplo, el primero es la esquina superior izquierda. En este diseño la esquina superior izquierda
	 * tiene comida. Asi que haremos la siguiente suma: 
	 * 										1 + 2 + 16 = 19
	 * 1 porque tiene borde izquierdo, 2 porque tiene borde derecho y 16 porque tiene comida adentro. 
	 * 
	 * Nota: Todos los bloques que sean obstaculo deben estar rodeados por bordes. Es decir que los bloques
	 * circundantes a una bloque obstaculo van a sumar borde del lado de donde el bloque obstáculo se encuentre.
	 */
	private final short levelData[] = {
			19, 26, 26, 18, 26, 26, 26, 18, 26, 26, 26, 18, 26, 26, 22,
            21, 0, 0, 21, 0, 0, 0, 21, 0, 0, 0, 21, 0, 0, 21,
            17, 26, 26, 16, 26, 18, 26, 24, 26, 18, 26, 16, 26, 26, 20,
            21, 0, 0, 21, 0, 21, 0, 0, 0, 21, 0, 21, 0, 0, 21,
            25, 26, 26, 20, 0, 25, 26, 18, 26, 28, 0, 17, 26, 26, 28,
            0, 0, 0, 21, 0, 0, 0, 21, 0, 0, 0, 21, 0, 0, 0,
            0, 0, 0, 17, 26, 26, 26, 16, 26, 26, 26, 20, 0, 0, 0,
            0, 0, 0, 21, 0, 0, 0, 5, 0, 0, 0, 21, 0, 0, 0,
            19, 26, 26, 20, 0, 11, 10, 8, 10, 14, 0, 17, 26, 26, 22,
            21, 0, 0, 21, 0, 0, 0, 0, 0, 0, 0, 21, 0, 0, 21,
            25, 22, 0, 17, 26, 18, 26, 26, 26, 18, 26, 20, 0, 19, 28,
            0, 21, 0, 21, 0, 21, 0, 0, 0, 21, 0, 21, 0, 21, 0,
            19, 24, 26, 28, 0, 25, 22, 0, 19, 28, 0, 25, 26, 24, 22,
            21, 0, 0, 0, 0, 0, 21, 0, 21, 0, 0, 0, 0, 0, 21,
            25, 26, 26, 26, 26, 26, 24, 26, 24, 26, 26, 26, 26, 26, 28
	};
	
	public Background() {
		loadImages();
		initVariables();
		addKeyListener(new TAdapter());
		setFocusable(true);
		initGame();
	}
	
	private void loadImages() {
		//Carga los sprites
		down = new ImageIcon("src/res/images/down.gif").getImage();
		up = new ImageIcon("src/res/images/up.gif").getImage();
		left = new ImageIcon("src/res/images/left.gif").getImage();
		right = new ImageIcon("src/res/images/right.gif").getImage();
		ghost = new ImageIcon("src/res/images/ghost.gif").getImage();
		heart = new ImageIcon("src/res/images/heart.png").getImage();
	}
	
	private void initVariables() {
		//Inicializa variables que vimos arriba
		screenData = new short[N_BLOCKS * N_BLOCKS];
		d = new Dimension(400,400);
		ghost_x = new int [MAX_GHOSTS];
		ghost_dx = new int [MAX_GHOSTS];
		ghost_y = new int [MAX_GHOSTS];
		ghost_dy = new int [MAX_GHOSTS];
		ghostSpeed = new int [MAX_GHOSTS];
		dx = new int[4];
		dy = new int[4];
		
		timer = new Timer(60, this); //Esto se encarga de las animaciones de los sprites.
		timer.restart();
	}
	
	private void initGame() {
		//Inicia el juego
		lives = 3;
		score = 0;
		initLevel();
		N_GHOSTS = 4;
		currentSpeed = 2;
		
	}
	
	public void initLevel() {
		//Inicializa el tablero
		int i;
		for(i = 0; i<N_BLOCKS * N_BLOCKS; i++) {
			screenData[i] = levelData[i];
		}
		continueLevel();
	}
	
	private void continueLevel() {
		//Paso siguiente inicializar el nivel, se define la cantidad de fantasmas y otros valores como
		//la velocidad, dirección y posición inicial de cada elemento del juego
		int dx = 1;
		int random;
		
		for (int i = 0; i < N_GHOSTS; i++) {
			ghost_y[i] = 6 * BLOCK_SIZE;
			ghost_x[i] = 7 * BLOCK_SIZE;
			ghost_dy[i] = 0;
			ghost_dx[i] = dx;
			dx = -dx;
			random = (int) (Math.random() * (currentSpeed + 1));
			
			if (random > currentSpeed) {
				random = currentSpeed;
			}
			
			ghostSpeed[i] = validSpeeds[random];
		}
		
		pacman_x = 7 *BLOCK_SIZE; 	//Posición donde arranca Pacman
		pacman_y = 10 *BLOCK_SIZE;
		pacmand_x = 0;				//Resetea la dirección de Pacman
		pacmand_y = 0;
		req_dx = 0;					//Resetea la dirección de los controles de Pacman
		req_dy = 0;
		dying = false;
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2d = (Graphics2D) g;
		
		g2d.setColor(Color.black);
		g2d.fillRect(0, 0, d.width, d.height);
		
		drawMaze(g2d);
		drawScore(g2d);
		
		if (inGame) {
			playGame(g2d);
		} else {
			showIntroScreen(g2d);
			g2d.dispose();
		}
		
	}
	
	private void drawMaze(Graphics2D g2d) {
		
		int i = 0;
        int x, y;

        for (y = 0; y < SCREEN_SIZE; y += BLOCK_SIZE) {
            for (x = 0; x < SCREEN_SIZE; x += BLOCK_SIZE) {

                g2d.setColor(new Color(0, 72, 251));
                g2d.setStroke(new BasicStroke(5));

                if ((screenData[i] & 1) != 0) {
                    g2d.drawLine(x, y, x, y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 2) != 0) {
                    g2d.drawLine(x, y, x + BLOCK_SIZE - 1, y);
                }

                if ((screenData[i] & 4) != 0) {
                    g2d.drawLine(x + BLOCK_SIZE - 1, y, x + BLOCK_SIZE - 1,
                            y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 8) != 0) {
                    g2d.drawLine(x, y + BLOCK_SIZE - 1, x + BLOCK_SIZE - 1,
                            y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 16) != 0) {
                    g2d.setColor(new Color(255, 255, 255));
                    g2d.fillOval(x + 10, y + 10, 4, 4);
                }

                i++;
            }
        }
    }

	private void drawScore(Graphics2D g2d) {
		g2d.setFont(smallfont);
		g2d.setColor(new Color(5, 151, 79));
		String s = "Puntuacion: " + score;
		g2d.drawString(s, SCREEN_SIZE / 2 + 96, SCREEN_SIZE + 16);
		
		for (int i = 0; i<lives; i++) {
			g2d.drawImage(heart, i * 28 + 8, SCREEN_SIZE + 1, this);
		}
	}
	
	private void playGame(Graphics2D g2d) {
		if(dying) {
			death();
		} else {
			movePacman();
			drawPacman(g2d);
			moveGhosts(g2d);
			checkMaze();
		}
	}
	
	private void death() {
		lives--;
		if(lives == 0) {
			inGame = false;
		}
		continueLevel();
	}
	
	private void movePacman() {

        int pos;
        short ch;

        if (pacman_x % BLOCK_SIZE == 0 && pacman_y % BLOCK_SIZE == 0) {
            pos = pacman_x / BLOCK_SIZE + N_BLOCKS * (int) (pacman_y / BLOCK_SIZE);
            ch = screenData[pos];

            if ((ch & 16) != 0) {
                screenData[pos] = (short) (ch & 15);
                score++;
            }

            if (req_dx != 0 || req_dy != 0) {
                if (!((req_dx == -1 && req_dy == 0 && (ch & 1) != 0)
                        || (req_dx == 1 && req_dy == 0 && (ch & 4) != 0)
                        || (req_dx == 0 && req_dy == -1 && (ch & 2) != 0)
                        || (req_dx == 0 && req_dy == 1 && (ch & 8) != 0))) {
                    pacmand_x = req_dx;
                    pacmand_y = req_dy;
                }
            }

            if ((pacmand_x == -1 && pacmand_y == 0 && (ch & 1) != 0)
                    || (pacmand_x == 1 && pacmand_y == 0 && (ch & 4) != 0)
                    || (pacmand_x == 0 && pacmand_y == -1 && (ch & 2) != 0)
                    || (pacmand_x == 0 && pacmand_y == 1 && (ch & 8) != 0)) {
                pacmand_x = 0;
                pacmand_y = 0;
            }
        }
        pacman_x = pacman_x + PACMAN_SPEED * pacmand_x;
        pacman_y = pacman_y + PACMAN_SPEED * pacmand_y;
    }
	
	private void drawPacman(Graphics2D g2d) {

        if (req_dx == -1) {
            g2d.drawImage(left, pacman_x + 1, pacman_y + 1, this);
        } else if (req_dx == 1) {
        	g2d.drawImage(right, pacman_x + 1, pacman_y + 1, this);
        } else if (req_dy == -1) {
        	g2d.drawImage(up, pacman_x + 1, pacman_y + 1, this);
        } else {
        	g2d.drawImage(down, pacman_x + 1, pacman_y + 1, this);
        }
    }
	
	private void moveGhosts(Graphics2D g2d) {
		int i;
        int pos;
        int count;

        for (i = 0; i < N_GHOSTS; i++) {
            if (ghost_x[i] % BLOCK_SIZE == 0 && ghost_y[i] % BLOCK_SIZE == 0) {
                pos = ghost_x[i] / BLOCK_SIZE + N_BLOCKS * (int) (ghost_y[i] / BLOCK_SIZE);

                count = 0;
                
                //Determinamos como se pueden mover los fantasmitas
                if ((screenData[pos] & 1) == 0 && ghost_dx[i] != 1) {
                    dx[count] = -1;
                    dy[count] = 0;
                    count++;
                }

                if ((screenData[pos] & 2) == 0 && ghost_dy[i] != 1) {
                    dx[count] = 0;
                    dy[count] = -1;
                    count++;
                }

                if ((screenData[pos] & 4) == 0 && ghost_dx[i] != -1) {
                    dx[count] = 1;
                    dy[count] = 0;
                    count++;
                }

                if ((screenData[pos] & 8) == 0 && ghost_dy[i] != -1) {
                    dx[count] = 0;
                    dy[count] = 1;
                    count++;
                }

                if (count == 0) {

                    if ((screenData[pos] & 15) == 15) {
                        ghost_dx[i] = 0;
                        ghost_dy[i] = 0;
                    } else {
                        ghost_dx[i] = -ghost_dx[i];
                        ghost_dy[i] = -ghost_dy[i];
                    }

                } else {

                    count = (int) (Math.random() * count);

                    if (count > 3) {
                        count = 3;
                    }

                    ghost_dx[i] = dx[count];
                    ghost_dy[i] = dy[count];
                }

            }

            ghost_x[i] = ghost_x[i] + (ghost_dx[i] * ghostSpeed[i]);
            ghost_y[i] = ghost_y[i] + (ghost_dy[i] * ghostSpeed[i]);
            drawGhost(g2d, ghost_x[i] + 1, ghost_y[i] + 1);
            
            //Colision
            if (pacman_x > (ghost_x[i] - 12) && pacman_x < (ghost_x[i] + 12)
                    && pacman_y > (ghost_y[i] - 12) && pacman_y < (ghost_y[i] + 12)
                    && inGame) {

                dying = true;
            }
        }
    }
	
	private void drawGhost(Graphics2D g2d, int x, int y) {
		g2d.drawImage(ghost, x, y, this);
	}
	
	private void checkMaze() {
		int i = 0;
        boolean finished = true;

        while (i < N_BLOCKS * N_BLOCKS && finished) {

            if ((screenData[i]) != 0) {
                finished = false;
            }

            i++;
        }

        if (finished) {

            score += 50;

            if (N_GHOSTS < MAX_GHOSTS) {
                N_GHOSTS++;
            }

            if (currentSpeed < maxSpeed) {
                currentSpeed++;
            }

            initLevel();
        }
	}
	
	private void showIntroScreen(Graphics2D g2d) {
		String start = "Apreta ESPACIO para comenzar";
		g2d.setColor(Color.yellow);
		g2d.drawString(start, (SCREEN_SIZE)/ 4, 150);
	}
	
	class TAdapter extends KeyAdapter {
		
		@Override
		public void keyPressed(KeyEvent e) {
			int key = e.getKeyCode();
			
			if(inGame) {
				if(key == KeyEvent.VK_LEFT) {
					req_dx = -1;
					req_dy = 0;
				}
				else if(key == KeyEvent.VK_RIGHT) {
					req_dx = 1;
					req_dy = 0;
				}
				else if(key == KeyEvent.VK_UP) {
					req_dx = 0;
					req_dy = -1;
				}
				else if(key == KeyEvent.VK_DOWN) {
					req_dx = 0;
					req_dy = 1;
				}
				else if(key == KeyEvent.VK_ESCAPE && timer.isRunning()) {
					inGame = false;
				}
			}else {
				if(key == KeyEvent.VK_SPACE) {
					inGame = true;
					initGame();
				}
			}
		}
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		repaint();
	}

}
