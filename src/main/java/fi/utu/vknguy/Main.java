package fi.utu.vknguy;

public class Main {

    public static final int MAX_REQUESTS = 16;


    // ============================================================
    // CPU
    // ============================================================

    public static void cpu(
            Cache cache,
            Request[] requests) {

        if (requests.length > MAX_REQUESTS) {
            throw new IllegalArgumentException(
                    "max 16 memory requests"
            );
        }

        for (Request request : requests) {

            cache.access(
                    request.write,
                    request.address,
                    request.dataOut
            );
        }
    }


    // ============================================================
    // Tests
    // ============================================================

    public static void runTests() {

        DRAM dram = new DRAM();

        Cache cache = new Cache(
                dram,
                1,
                false
        );


        // --------------------------------------------------------
        // Read miss, then read hit
        // --------------------------------------------------------

        AccessResult result;

        result = cache.access(
                0,
                0x34,
                null
        );

        assert !result.hit;
        assert result.data == 52;


        result = cache.access(
                0,
                0x34,
                null
        );

        assert result.hit;
        assert result.data == 52;


        // --------------------------------------------------------
        // Write hit updates cache and DRAM
        // --------------------------------------------------------

        result = cache.access(
                1,
                0x34,
                99
        );

        assert result.hit;
        assert result.data == null;

        assert dram.access(
                0,
                0x34,
                null
        ) == 99;


        result = cache.access(
                0,
                0x34,
                null
        );

        assert result.hit;
        assert result.data == 99;


        // --------------------------------------------------------
        // 0x24 and 0x34 have the same index
        // --------------------------------------------------------

        result = cache.access(
                0,
                0x24,
                null
        );

        assert !result.hit;
        assert result.data == 36;


        result = cache.access(
                0,
                0x34,
                null
        );

        assert !result.hit;
        assert result.data == 99;


        // --------------------------------------------------------
        // Write miss
        // --------------------------------------------------------

        result = cache.access(
                1,
                0x18,
                77
        );

        assert !result.hit;
        assert result.data == null;


        result = cache.access(
                0,
                0x18,
                null
        );

        assert !result.hit;
        assert result.data == 77;


        result = cache.access(
                0,
                0x18,
                null
        );

        assert result.hit;
        assert result.data == 77;


        // --------------------------------------------------------
        // First and last addresses
        // --------------------------------------------------------

        int[] addresses = {
                0, 15, 16, 63
        };

        for (int address : addresses) {

            result = cache.access(
                    0,
                    address,
                    null
            );

            assert result.data
                    == dram.access(
                    0,
                    address,
                    null
            );
        }


        // --------------------------------------------------------
        // Bad requests
        // --------------------------------------------------------

        Request[] badRequests = {

                new Request(0, -1, null),

                new Request(0, 64, null),

                new Request(2, 0, null),

                new Request(1, 0, 256)
        };


        for (Request request : badRequests) {

            boolean rejected = false;

            try {

                cache.access(
                        request.write,
                        request.address,
                        request.dataOut
                );

            } catch (IllegalArgumentException e) {

                rejected = true;
            }

            assert rejected
                    : "bad request was accepted";
        }


        // --------------------------------------------------------
        // More than 16 requests
        // --------------------------------------------------------

        boolean rejected = false;

        try {

            Request[] tooMany =
                    new Request[17];

            for (int i = 0; i < 17; i++) {

                tooMany[i] =
                        new Request(
                                0,
                                0,
                                null
                        );
            }

            cpu(cache, tooMany);

        } catch (IllegalArgumentException e) {

            rejected = true;
        }

        assert rejected
                : "more than 16 requests were accepted";


        // --------------------------------------------------------
        // LRU with 2 ways
        // --------------------------------------------------------

        Cache lru = new Cache(
                new DRAM(),
                2,
                false
        );

        int[] lruAddresses = {
                0, 8, 0, 16, 0, 8
        };

        boolean[] results =
                new boolean[6];


        for (int i = 0;
             i < lruAddresses.length;
             i++) {

            result = lru.access(
                    0,
                    lruAddresses[i],
                    null
            );

            results[i] = result.hit;
        }


        boolean[] expected = {
                false,
                false,
                true,
                false,
                true,
                false
        };


        for (int i = 0;
             i < results.length;
             i++) {

            assert results[i]
                    == expected[i];
        }


        // Check tags
        int[] tags =
                lru.getTagStore(0);

        assert tags[0] == 0;
        assert tags[1] == 1;


        // Check valid bits
        boolean[] valid =
                lru.getValid(0);

        assert valid[0];
        assert valid[1];


        System.out.println(
                "Self-check passed: reads, writes, "
                        + "eviction, LRU and input limits"
        );
    }


    // ============================================================
    // MAIN
    // ============================================================

    public static void main(String[] args) {

        if (args.length == 0) {

            // ----------------------------------------------------
            // Task 3: direct mapped cache
            // ----------------------------------------------------

            Request[] requests = {

                    new Request(
                            0, 0x34, null
                    ),

                    new Request(
                            0, 0x34, null
                    ),

                    new Request(
                            1, 0x34, 99
                    ),

                    new Request(
                            0, 0x34, null
                    ),

                    new Request(
                            0, 0x24, null
                    ),

                    new Request(
                            0, 0x34, null
                    ),

                    new Request(
                            1, 0x18, 77
                    ),

                    new Request(
                            0, 0x18, null
                    ),

                    new Request(
                            0, 0x18, null
                    ),

                    new Request(
                            0, 0x08, null
                    ),

                    new Request(
                            0, 0x18, null
                    ),

                    new Request(
                            0, 0x00, null
                    ),

                    new Request(
                            1, 0x00, 11
                    ),

                    new Request(
                            0, 0x00, null
                    ),

                    new Request(
                            0, 0x10, null
                    ),

                    new Request(
                            0, 0x00, null
                    )
            };


            cpu(
                    new Cache(new DRAM()),
                    requests
            );


        } else if (args[0].equals("--check")) {

            runTests();


        } else if (args[0].equals("--lru")) {

            // ----------------------------------------------------
            // Task 4: 2-way set associative cache
            // ----------------------------------------------------

            int[] addresses = {
                    0, 8, 0, 16, 0, 8
            };

            Request[] requests =
                    new Request[addresses.length];


            for (int i = 0;
                 i < addresses.length;
                 i++) {

                requests[i] =
                        new Request(
                                0,
                                addresses[i],
                                null
                        );
            }


            cpu(
                    new Cache(
                            new DRAM(),
                            2
                    ),
                    requests
            );


        } else {

            System.out.println(
                    "Usage: java Main [--check | --lru]"
            );
        }
    }
}

