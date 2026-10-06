package fi.utu.vknguy;

public class Cache {

    public static final int CACHE_BLOCKS = 16;

    private DRAM dram;
    private int ways;
    private int numSets;
    private boolean show;

    // Cache data
    private int[][] dataStore;

    // Tags
    private int[][] tagStore;

    // Valid bits
    private boolean[][] valid;

    // Last-used time for LRU
    private int[][] lastUsed;

    private int clock;


    public Cache(DRAM dram) {
        this(dram, 1, true);
    }


    public Cache(DRAM dram, int ways) {
        this(dram, ways, true);
    }


    public Cache(DRAM dram, int ways, boolean show) {

        if (ways != 1 && ways != 2) {
            throw new IllegalArgumentException(
                    "ways must be 1 or 2"
            );
        }

        this.dram = dram;
        this.ways = ways;

        // 16 sets for direct mapped
        // 8 sets for 2-way
        this.numSets = CACHE_BLOCKS / ways;

        this.show = show;

        dataStore = new int[numSets][ways];
        tagStore = new int[numSets][ways];
        valid = new boolean[numSets][ways];
        lastUsed = new int[numSets][ways];

        clock = 0;
    }


    // ------------------------------------------------------------
    // Check request
    // ------------------------------------------------------------

    public void checkRequest(
            int write,
            int address,
            Integer dataOut) {

        if (write != 0 && write != 1) {
            throw new IllegalArgumentException(
                    "read/write must be 0 or 1"
            );
        }

        if (address < 0 || address >= DRAM.DRAM_SIZE) {
            throw new IllegalArgumentException(
                    "address must be 0x00 - 0x3F"
            );
        }

        if (write == 1) {

            if (dataOut == null
                    || dataOut < 0
                    || dataOut > 255) {

                throw new IllegalArgumentException(
                        "write data must be 0 - 255"
                );
            }
        }
    }


    // ------------------------------------------------------------
    // Find matching way
    // ------------------------------------------------------------

    public int findWay(int index, int tag) {

        for (int way = 0; way < ways; way++) {

            if (valid[index][way]
                    && tagStore[index][way] == tag) {

                return way;
            }
        }

        return -1;
    }


    // ------------------------------------------------------------
    // Choose a way
    // ------------------------------------------------------------

    public int chooseWay(int index) {

        // First look for an empty way
        for (int way = 0; way < ways; way++) {

            if (!valid[index][way]) {
                return way;
            }
        }

        // All ways are full.
        // Find least recently used way.

        int oldest = 0;

        for (int way = 1; way < ways; way++) {

            if (lastUsed[index][way]
                    < lastUsed[index][oldest]) {

                oldest = way;
            }
        }

        return oldest;
    }


    // ------------------------------------------------------------
    // Cache access
    // ------------------------------------------------------------

    public AccessResult access(
            int write,
            int address,
            Integer dataOut) {

        checkRequest(write, address, dataOut);

        // Calculate index and tag
        int index = address % numSets;
        int tag = address / numSets;

        clock++;

        int way = findWay(index, tag);

        boolean hit = (way != -1);

        Integer dataIn = null;


        // ========================================================
        // WRITE
        // ========================================================

        if (write == 1) {

            // Write-through:
            // Always update DRAM.
            dram.access(1, address, dataOut);

            if (hit) {

                // Also update cache
                dataStore[index][way] = dataOut;

                lastUsed[index][way] = clock;
            }

            // Write miss:
            // Do not put anything into cache.
        }


        // ========================================================
        // READ
        // ========================================================

        else {

            if (!hit) {

                // Read miss:
                // Get data from DRAM.

                way = chooseWay(index);

                dataStore[index][way] =
                        dram.access(0, address, null);

                tagStore[index][way] = tag;

                valid[index][way] = true;
            }

            lastUsed[index][way] = clock;

            dataIn = dataStore[index][way];
        }


        // ========================================================
        // Print
        // ========================================================

        if (show) {

            String status;

            if (hit) {
                status = "HIT ";
            } else {
                status = "MISS";
            }

            if (write == 1) {

                System.out.printf(
                        "0x%02X %s WRITE %d%n",
                        address,
                        status,
                        dataOut
                );

            } else {

                System.out.printf(
                        "0x%02X %s READ -> %d%n",
                        address,
                        status,
                        dataIn
                );
            }
        }

        return new AccessResult(hit, dataIn);
    }


    // ------------------------------------------------------------
    // Getters for testing
    // ------------------------------------------------------------

    public int[] getTagStore(int index) {
        return tagStore[index];
    }

    public boolean[] getValid(int index) {
        return valid[index];
    }
}

