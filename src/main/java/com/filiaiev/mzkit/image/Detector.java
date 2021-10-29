package com.filiaiev.mzkit.image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.List;
import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class Detector {

    public static BufferedImage detectObjects(BufferedImage img, int[][] binarizedMatrix) {
        ColorModel imgColorModel = img.getColorModel();
        WritableRaster imgRaster = img.getRaster();
        boolean imgAlphaPremultiplied = img.isAlphaPremultiplied();

        BufferedImage result = new BufferedImage(imgColorModel, imgRaster, imgAlphaPremultiplied, null);

        int objectIndex = 2;

        for (int i = 0; i < binarizedMatrix.length; i++) {
            for (int j = 0; j < binarizedMatrix[0].length; j++) {
                if(binarizedMatrix[i][j] == 1) {
                    binarizedMatrix[i][j] = objectIndex;
                    HashSet<Pixel> infectedPixels = new HashSet<>();
                    infectedPixels.add(new Pixel(i, j));

                    infect(i, j, binarizedMatrix, infectedPixels);

                    // System.out.println("Infected size: " + infectedPixels.size());
                    int fill = infectedPixels.size() > 1000 ? objectIndex : 0;

                    if(fill > 0) {
                        List<Pixel> boundaries = getBoundaries(infectedPixels);

                        for (Pixel p : infectedPixels) {
                            binarizedMatrix[p.getX()][p.getY()] = fill;
                        }

                        int xTop = max(boundaries.get(1).getX()-1, 0);
                        int yTop = max(boundaries.get(2).getY()-1, 0);
                        int xBottom = min(boundaries.get(0).getX()+1, binarizedMatrix.length-1);
                        int yBottom = min(boundaries.get(3).getY()+1, binarizedMatrix[0].length-1);
                        // System.out.println("xTop: " + xTop + ", yTop: " + yTop + ", xBot: " + xBottom + ", yBot: " + yBottom);

                        for (int k = xTop; k < xBottom; k++) {
                            binarizedMatrix[k][yTop] = -1;
                            binarizedMatrix[k][yBottom] = -1;
                        }

                        for (int k = yTop; k < yBottom; k++) {
                            binarizedMatrix[xTop][k] = -1;
                            binarizedMatrix[xBottom][k] = -1;
                        }

                        Graphics2D graphics = result.createGraphics();
                        graphics.setColor(Color.RED);
                        System.out.println("xTop = " + xTop +", yTop = " + yTop + ", xBot = " + xBottom + ", yBot = " + yBottom);
                        System.out.println("==========");
                        graphics.drawRect(xTop, yTop, xBottom-xTop, yBottom-yTop);
                        objectIndex++;
                    }else {
                        for (Pixel p : infectedPixels) {
                            binarizedMatrix[p.getX()][p.getY()] = fill;
                        }
                    }
                }
            }
        }

        return result;
    }

    private static void infect(int i, int j, int[][] pixelMatrix, Set<Pixel> infectedPixels) {
        // Checking top pixel
        int moveIndex = min(j+1, pixelMatrix[0].length-1);
        Pixel pixel = new Pixel(i, moveIndex);
        if(!infectedPixels.contains(pixel) && pixelMatrix[i][moveIndex] == 1) {
            infectedPixels.add(pixel);
            infect(i, moveIndex, pixelMatrix, infectedPixels);
        }

        // Checking bottom pixel
        moveIndex = max(j-1, 0);
        pixel = new Pixel(i, moveIndex);
        if(!infectedPixels.contains(pixel) && pixelMatrix[i][moveIndex] == 1) {
            infectedPixels.add(pixel);
            infect(i, moveIndex, pixelMatrix, infectedPixels);
        }

        // Checking left pixel
        moveIndex = max(i-1, 0);
        pixel = new Pixel(moveIndex, j);
        if(!infectedPixels.contains(pixel) && pixelMatrix[moveIndex][j] == 1) {
            infectedPixels.add(pixel);
            infect(moveIndex, j, pixelMatrix, infectedPixels);
        }

        // Checking right pixel
        moveIndex = min(i+1, pixelMatrix.length-1);
        pixel = new Pixel(moveIndex, j);
        if(!infectedPixels.contains(pixel) && pixelMatrix[moveIndex][j] == 1) {
            infectedPixels.add(pixel);
            infect(moveIndex, j, pixelMatrix, infectedPixels);
        }

    }

    // Отримуємо границі знайденого об'єкту
    private static List<Pixel> getBoundaries(Set<Pixel> infectedPixels) {
        List<Pixel> boundaries;

        // Знаходження границь відбувається шляком пошуку макс та мін координат
        Optional<Pixel> rightBoundary = infectedPixels.stream().max(Comparator.comparingInt(Pixel::getX));
        Optional<Pixel> leftBoundary = infectedPixels.stream().min(Comparator.comparingInt(Pixel::getX));
        Optional<Pixel> topBoundary = infectedPixels.stream().min(Comparator.comparingInt(Pixel::getY));
        Optional<Pixel> bottomBoundary = infectedPixels.stream().max(Comparator.comparingInt(Pixel::getY));

        boundaries = Arrays.asList(rightBoundary.get(), leftBoundary.get(), topBoundary.get(), bottomBoundary.get());
        return boundaries;
    }

}
