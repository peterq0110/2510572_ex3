package fi.utu.vknguy;

public class Main_ex4 {

    public static void main(String[] args) {

        // 2-way set associative cache
        Cache lru = new Cache(
                new DRAM(),
                2,
                true
        );


        // 0x00, 0x08 and 0x10
        // all go to set 0

        int[] addresses = {
                0, 8, 0, 16, 0, 8
        };


        for (int address : addresses) {

            lru.access(
                    0,
                    address,
                    null
            );
        }
    }
}

