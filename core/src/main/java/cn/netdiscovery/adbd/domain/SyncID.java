package cn.netdiscovery.adbd.domain;

public enum SyncID {

    LSTAT_V1("STAT"),
    STAT_V2("STA2"),
    LSTAT_V2("LST2"),

    LIST_V1("LIST"),
    LIST_V2("LIS2"),
    DENT_V1("DENT"),
    DENT_V2("DNT2"),

    SEND_V1("SEND"),
    SEND_V2("SND2"),
    RECV_V1("RECV"),
    RECV_V2("RCV2"),

    DONE("DONE"),
    DATA("DATA"),
    OKAY("OKAY"),
    FAIL("FAIL"),
    QUIT("QUIT"),

    ;

    private long value;

    private byte[] array;

    SyncID(String code) {
        this.array = new byte[4];
        for(int i=0; i<code.length(); i++) {
            this.array[i] = (byte) code.charAt(i);
        }
        this.value = ((this.array[0]) | ((this.array[1]) << 8) | ((this.array[2]) << 16) | ((this.array[3]) << 24)) & 0xFFFFFFFF;
    }

    public byte[] byteArray() {
        return array;
    }

    public static SyncID findByValue(int value) {
        long longValue = value & 0xFFFFFFFF;
        return findByValue(longValue);
    }

    public static SyncID findByValue(long value) {
        for(SyncID id : values()) {
            if (id.value == value) {
                return id;
            }
        }
        return null;
    }
}
