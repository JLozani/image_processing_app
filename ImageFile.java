/**
  * Names: Luka Pendo, Ivo Masar, Josip Peter Lozancic
  * Course: ISTE-121-700
  * Group Project: Team #2
  * Description: ImageFile class
  *
  * @version 04/01/2020
  */

import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class ImageFile implements Serializable {
   private int choice;
   private String name;
   private int[][] pixels;
   private int type;
   
   public ImageFile(int _choice, String _name, BufferedImage image) {
      choice = _choice;
      name = _name;
      type = image.getType();
      pixels = new int[image.getWidth()][image.getHeight()];
      
      for(int i = 0; i < pixels.length; i++) {
         for(int j = 0; j < pixels[0].length; j++) {
            pixels[i][j] = image.getRGB(i, j);
         }
      }
   }
   
   public int getChoice() { return choice; }
   public String getName() { return name; }
   public int getWidth() { return pixels.length; }
   public int getHeight() { return (pixels != null ? pixels[0].length : 0); }
   public int[][] getPixels() { return pixels; }
   
   public BufferedImage getBufferedImage() { 
      BufferedImage temp = new BufferedImage(pixels.length, pixels[0].length, type);
      
      temp.setRGB(0, 0, pixels.length, pixels[0].length, DoubleBreak(), 0, pixels.length);
      
      return temp;
   }
   
   private int[] DoubleBreak() {
      int[] array = new int[pixels.length * pixels[0].length];

      for(int i = 0; i < array.length; i++) {
         array[i] = pixels[i % pixels.length][i / pixels.length];
      }
      
      return array;
   }
   
   public void write() throws Exception {
      FileOutputStream fos = new FileOutputStream(name);
      
      ImageIO.write(getBufferedImage(), "jpg", fos);
      
      fos.close();
   }
   
   public void process() {
      switch(choice) {
         case 1:
            name = name.substring(0, name.length() - 4) + "_grayscale.jpg";
            break;
         case 2:
            name = name.substring(0, name.length() - 4) + "_negative.jpg";
            break;
         case 3:
            name = name.substring(0, name.length() - 4) + "_sepia.jpg";
            break;
      }
      
      if(pixels.length * pixels[0].length < 36) {
         int xPart = pixels.length / 3;
         int yPart = pixels[0].length / 2;
         
         Thread[] threads = { new ProcessPart(0, 0, xPart, yPart),
                              new ProcessPart(xPart, 0, xPart * 2, yPart),
                              new ProcessPart(xPart * 2, 0, pixels.length, yPart),
                              new ProcessPart(0, yPart, xPart, pixels[0].length),
                              new ProcessPart(xPart, yPart, xPart * 2, pixels[0].length),
                              new ProcessPart(xPart * 2, yPart, pixels.length, pixels[0].length) };
         
         for(Thread thread : threads) {
            thread.start();
         }
         
         try {
            for(Thread thread : threads) {
               thread.join();
            }
         } catch(Exception e) {}
      } else {
         Thread thread = new ProcessPart(0, 0, pixels.length, pixels[0].length);
         
         thread.start();
         
         try {
            thread.join();
         } catch(Exception e) {}
      }
   }
      
   class ProcessPart extends Thread {
      int xStart, yStart, xEnd, yEnd;
   
      public ProcessPart(int _xStart, int _yStart, int _xEnd, int _yEnd) {
         xStart = _xStart;
         yStart = _yStart;
         xEnd = _xEnd;
         yEnd = _yEnd;
      }
      
      public void start() {
         int p, a, r, g, b;
         
         switch(choice) {
            case 1:
               int avg;
               
               for(int i = xStart; i < xEnd; i++) {
                  for(int j = yStart; j < yEnd; j++) {
                     p = pixels[i][j];
                     a = (p >> 24) & 0xff;
                     r = (p >> 16) & 0xff;
                     g = (p >> 8) & 0xff;
                     b = p & 0xff;
                     
                     avg = (r + g + b) / 3;
                     
                     pixels[i][j] = (a << 24) | (avg << 16) | (avg << 8) | avg;
                  }
               }
               
               break;
            case 2:
               for(int i = xStart; i < xEnd; i++) {
                  for(int j = yStart; j < yEnd; j++) {
                     p = pixels[i][j];
                     a = (p >> 24) & 0xff;
                     r = (p >> 16) & 0xff;
                     g = (p >> 8) & 0xff;
                     b = p & 0xff;
                     
                     pixels[i][j] = (a << 24) | ((255 - r) << 16) | ((255 - g) << 8) | (255 - b);
                  }
               }
               
               break;
            case 3:
               int r_new, g_new, b_new;
               
               for(int i = xStart; i < xEnd; i++) {
                  for(int j = yStart; j < yEnd; j++) {
                     p = pixels[i][j];
                     a = (p >> 24) & 0xff;
                     r = (p >> 16) & 0xff;
                     g = (p >> 8) & 0xff;
                     b = p & 0xff;
                     
                     r_new = (int)(0.393 * r + 0.769 * g + 0.189 * b);
                     g_new = (int)(0.349 * r + 0.686 * g + 0.168 * b);
                     b_new = (int)(0.272 * r + 0.534 * g + 0.131 * b);
                     
                     if(r_new > 255) r_new = 255;
                     if(g_new > 255) g_new = 255;
                     if(b_new > 255) b_new = 255;
                     
                     pixels[i][j] = (a << 24) | (r_new << 16) | (g_new << 8) | b_new;
                  }
               }
               
               break;
         }
      }
   }
}