package logical;

import javax.swing.JFrame;

public class Pacman extends JFrame{
	
	public Pacman() {
		add(new Background());
	}
	
	public static void main (String[] args) {
		Pacman pac = new Pacman();
		pac.setVisible(true);
		pac.setTitle("Pacman");
		pac.setSize(410, 420);
		pac.setDefaultCloseOperation(EXIT_ON_CLOSE);
		pac.setLocationRelativeTo(null);
	}
}
