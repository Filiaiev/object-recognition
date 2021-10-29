package com.filiaiev.mzkit.image;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;

public class ImageFilter {

    public static int[][] getBinarizedMatrix(BufferedImage img) {
        BufferedImage binarized = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null)
                .filter(img, null);
        int[][] matrix = new int[binarized.getWidth()][binarized.getHeight()];

        for (int i = 0; i < binarized.getWidth(); i++) {
            for (int j = 0; j < binarized.getHeight(); j++) {
                int rgb = binarized.getRGB(i, j) & 0xFF;
                if(rgb <= 100)
                    matrix[i][j] = 1;
                else
                    matrix[i][j] = 0;
            }
        }

        return matrix;
    }

}
