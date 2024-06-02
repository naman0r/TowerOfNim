package nimRecursion;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class NimGameGUI {
    private JFrame frame;
    private JPanel towerPanel;
    private JPanel twists;
    private JFrame insFrame;
    private JLabel insLabel; 
    private ArrayList<JButton> towerButtons = new ArrayList<>();
    private ArrayList<Tower> towers = new ArrayList<>();
    private boolean isPlayerTurn = true;
    private JButton computerMoveButton;
    private JButton earthquakeButton;
    private JButton instructionsButton;
    private JButton nextLevelButton;
    private int numTowers;
    private boolean earthquakeTried = false;

    public NimGameGUI(int n) {
        numTowers = n;
        frame = new JFrame("Towers of Logic, Naman Rusia's Recursion Project ATDS");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        nextLevelButton = new JButton("Next level here!");
        twists = new JPanel();
        twists.setLayout(new FlowLayout());
        frame.add(twists, BorderLayout.NORTH);

        instructionsButton = new JButton("Click here for instructions!");
        twists.add(instructionsButton);
        twists.setBackground(Color.BLACK);
        twists.add(nextLevelButton);

        towerPanel = new JPanel();
        towerPanel.setBackground(new Color(100,150,0));

        earthquakeButton = new JButton("EarthQuake!");

        towerPanel.setLayout(new FlowLayout());
        frame.add(towerPanel, BorderLayout.CENTER);

        for (int i = 0; i < n; i++) {
            towers.add(new Tower(new Random().nextInt(5) + 3));
        }
        updateTowerPanel();

        computerMoveButton = new JButton("Computer Move");
        
        instructionsButton.addActionListener(e -> {
        	showInstructions();
        });

        computerMoveButton.addActionListener(e -> {
            if (!isPlayerTurn) {
                computerMove();
                isPlayerTurn = true;
            }
        });

        earthquakeButton.addActionListener(e -> {
            if (!earthquakeTried) implementEarthquake(numTowers-1);
        });

        nextLevelButton.addActionListener(e -> {
            frame.dispose();
            new NimGameGUI(n+1);
            playNextLevelSound();
        });

        frame.add(computerMoveButton, BorderLayout.SOUTH);
        twists.add(earthquakeButton);

        frame.setVisible(true);
    }

    private void playNextLevelSound() {
        try {
            File soundFile = new File("/Users/namanrusia/Desktop/RecursionProject/nimRecursion/src/nimRecursion/nextLevel.wav");
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void playEarthquakeSound() {
        try {
            File soundFile = new File("/Users/namanrusia/Desktop/RecursionProject/nimRecursion/src/nimRecursion/earthquake.wav");
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void updateTowerPanel() {
        towerPanel.removeAll();
        towerButtons.clear();

        for (int i = 0; i < towers.size(); i++) {
            Tower tower = towers.get(i);
            JButton button = new JButton(tower.blocks + " blocks");
            button.setPreferredSize(new Dimension(100, tower.blocks * 30));
            button.setBackground(Color.GREEN);
            button.setForeground(Color.BLACK);
            int finalI = i;
            button.addActionListener(e -> {
                if (isPlayerTurn) {
                    String input = JOptionPane.showInputDialog("Blocks to remove from tower " + (finalI + 1) + ":");
                    if (input != null && !input.isEmpty()) {
                        try {
                            int blocksToRemove = Integer.parseInt(input);
                            if (blocksToRemove > 0 && blocksToRemove <= tower.blocks) {
                                playerMove(finalI, blocksToRemove);
                                isPlayerTurn = false;
                            }
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(frame, "Please enter a valid number!");
                        }
                    }
                }
            });
            towerButtons.add(button);
            towerPanel.add(button);
        }

        frame.revalidate();
        frame.repaint();
    }

    private void implementEarthquake(int n) {
        if (n == 0) {
            playEarthquakeSound();
            if (towers.get(n).getHeight() > 2) {
                towers.get(n).setHeight(towers.get(n).getHeight() - 2);
            } else {
                towers.get(n).setHeight(1);
            }
            earthquakeTried = true;
            updateTowerPanel();
        } else {
            if (towers.get(n).getHeight() > 2) {
                towers.get(n).setHeight(towers.get(n).getHeight() - 2);
            } else {
                towers.get(n).setHeight(1);
            }
            implementEarthquake(n - 1);
        }
    }

    private void playerMove(int towerIndex, int blocksToRemove) {
        Tower tower = towers.get(towerIndex);
        if (blocksToRemove >= tower.blocks) {
            JOptionPane.showMessageDialog(frame, "You cannot remove all the blocks from a tower!");
            
            return;
        }
        tower.blocks -= blocksToRemove; // Reduce the height of the primary tower
        growAdjacentTowers(towerIndex, 2); // Grow adjacent towers
        updateTowerPanel();
        checkEndGame();
    }

    private void computerMove() {
        Tower tallestTower = towers.get(0);
        int tallestTowerIndex = 0;
        for (int i = 0; i < towers.size(); i++) {
            if (towers.get(i).blocks > tallestTower.blocks) {
                tallestTower = towers.get(i);
                tallestTowerIndex = i;
            }
        }
        // Ensure the computer does not remove all blocks from the tower
        int blocksToRemove = new Random().nextInt(tallestTower.blocks); // This will now range from 0 to (tower height - 1)
        if (blocksToRemove == 0) { // If 0 blocks are selected to be removed, just remove 1 for simplicity
            blocksToRemove = 1;
        }
        tallestTower.blocks -= blocksToRemove; // Reduce the height of the primary tower
        growAdjacentTowers(tallestTowerIndex, 2); // Grow adjacent towers
        updateTowerPanel();
        JOptionPane.showMessageDialog(frame, "Computer removed " + blocksToRemove + " blocks from tower " + (tallestTowerIndex + 1) + "!");
        checkEndGame();
    }

    private void growAdjacentTowers(int towerIndex, int growthAmount) {
        // Base case: If growthAmount is 0, return
        if (growthAmount <= 0) return;

        // Adjust the towers immediately adjacent to the primary tower
        if (towerIndex > 0) {
            towers.get(towerIndex - 1).blocks += growthAmount;
        }
        if (towerIndex < towers.size() - 1) {
            towers.get(towerIndex + 1).blocks += growthAmount;
        }

        // Adjust the towers two away from the primary tower
        if (towerIndex > 1) {
            towers.get(towerIndex - 2).blocks += growthAmount - 1;
        }
        if (towerIndex < towers.size() - 2) {
            towers.get(towerIndex + 2).blocks += growthAmount - 1;
        }
    }


    private void checkEndGame() {
        for (Tower tower : towers) {
            if (tower.blocks >= 15) {
            	String winner; 
                if( isPlayerTurn == false) {
                	 winner = "Computer";
                }
                else winner = "Player";
                JOptionPane.showMessageDialog(frame, winner + " wins!");
                System.exit(0);
                new NimGameGUI(numTowers +1);
                playNextLevelSound();
                
            }
        }
    }
    
    public void showInstructions() {  //prints a new frame with instructions. 
  
    	
    	//instructions window
        insFrame = new JFrame("Towers of Logic Instructions:");
        
        insLabel = new JLabel("<html>This game is called 'The towers of Logic'. "
        		+ "<BR>This is a player V. computer game. Player plays first, after which, it will be the computer's turn. Press the 'computer turn' button to show what move the computer plays, and only then you will be able to play your next move. "
        		+ "<BR> The objective of this game is to beat the computer and be the first person to build a tower that is at least 15 levels tall. To do this, you have to remove levels from towers.  "
        		+ "<BR>Sounds confusing, right? Let me explain. You can choose to remove as many levels from any tower as you want (min height you can reduce a tower to is 1). "
        		+ "<BR>Once you remove levels from a tower of your choice. The Towers to the immediate left and right of the tower chosen will increase in height by 2. The towers one over from the tower chosen will grow in height by 1."
        		+ "<BR>So, to increase the height of towers, you must reduce the height of one tower first. Strategy is emminent in this game."
        		+ "<BR>At each level, you are allowed to use the 'Earthquake' feature only once. When used, the height of all the towers wil reduce by 2. This can get you out of a jam if the computer is at the verge of winning at two or more avanues!"
        		+ "<BR>You can increase your level at any time. When you choose to move on to the next level, then the number of towers will increase by 1. "
        		+ "<BR>The best way to learn this game is to play! Have fun, and good Luck! Close this window to continue to the game.</html>");  // html printing is used to accomplish the goal of having multi-line statements. <BR> is the name as /n in sysoutprint
        insFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        insFrame.setLayout(new BorderLayout());
        insFrame.add(insLabel, BorderLayout.NORTH);
        insFrame.setVisible(true);
        insFrame.setSize(600,500);
        

    }

    private class Tower {
        int blocks;

        Tower(int blocks) {
            this.blocks = blocks;
        }

        public int getHeight() {
            return this.blocks;
        }

        private void setHeight(int height) {
            this.blocks = height;
        }
    }
    

    public static void main(String[] args) {
        new NimGameGUI(5);
    }
}
