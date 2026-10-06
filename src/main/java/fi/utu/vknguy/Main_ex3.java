package fi.utu.vknguy;

public class Main_ex3 {

    public static final int MAX_REQUESTS = 16;

    public static void main(String[] args) {

        DRAM dram = new DRAM();

        // Direct mapped cache
        Cache cache = new Cache(dram, 1, false);

        AccessResult result;


        // --------------------------------------------------------
        // Read miss, then read hit
        // --------------------------------------------------------

        result = cache.access(0, 0x34, null);

        assert !result.hit;
        assert result.data == 52;


        result = cache.access(0, 0x34, null);

        assert result.hit;
        assert result.data == 52;


        // --------------------------------------------------------
        // Write hit updates cache and DRAM
        // --------------------------------------------------------

        result = cache.access(1, 0x34, 99);

        assert result.hit;
        assert result.data == null;

        assert dram.access(0, 0x34, null) == 99;


        result = cache.access(0, 0x34, null);

        assert result.hit;
        assert result.data == 99;


        // --------------------------------------------------------
        // 0x24 and 0x34 have the same index
        // --------------------------------------------------------

        result = cache.access(0, 0x24, null);

        assert !result.hit;
        assert result.data == 36;


        result = cache.access(0, 0x34, null);

        assert !result.hit;
        assert result.data == 99;


        // --------------------------------------------------------
        // Write miss
        // --------------------------------------------------------

        result = cache.access(1, 0x18, 77);

        assert !result.hit;
        assert result.data == null;


        result = cache.access(0, 0x18, null);

        assert !result.hit;
        assert result.data == 77;


        result = cache.access(0, 0x18, null);

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

            assert rejected :
                    "bad request was accepted";
        }


        // --------------------------------------------------------
        // More than 16 requests
        // --------------------------------------------------------

        boolean rejected = false;

        try {

            Request[] tooMany = new Request[17];

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

        assert rejected :
                "more than 16 requests were accepted";


        System.out.println(
                "Self-check passed!"
        );
    }


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
}

