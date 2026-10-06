package fi.utu.vknguy;

public class DRAM {

    public static final int DRAM_SIZE = 64;

    private int[][] data;

    public DRAM() {
        // 8 x 8 array
        data = new int[8][8];

        // Each cell starts with its own address
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                data[row][col] = row * 8 + col;
            }
        }
    }

    public int access(int write, int address, Integer dataOut) {

        // Upper 3 bits = row
        // Lower 3 bits = column
        int row = address / 8;
        int col = address % 8;

        if (write == 1) {
            data[row][col] = dataOut;
        }

        return data[row][col];
    }
}
