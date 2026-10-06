package fi.utu.vknguy;

public class Request {

    public int write;
    public int address;
    public Integer dataOut;

    public Request(int write, int address, Integer dataOut) {
        this.write = write;
        this.address = address;
        this.dataOut = dataOut;
    }
}

