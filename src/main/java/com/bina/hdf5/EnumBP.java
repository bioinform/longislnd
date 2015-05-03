package com.bina.hdf5;

/**
 * Created by bayo on 5/2/15.
 */
import java.util.Map;
import java.util.Arrays;
import java.util.EnumSet;
public enum EnumBP {
    //      value, cmp.h5's 4-bit code, ascii, character
    A      (0,     1,                   65,    'A'),
    C      (1,     2,                   67,    'C'),
    G      (2,     4,                   71,    'G'),
    T      (3,     8,                   84,    'T'),
    N      (4,     15,                  78,    'N'),
    Gap    (5,     0,                   32,    ' '),
    Invalid(6,     127,                 127,   '|');

    public static EnumBP cmp2ref(byte cmp){
        return cmp2ref_[cmp&0xff];
    }
    public static EnumBP cmp2read(byte cmp){
        return cmp2read_[cmp&0xff];
    }

    public byte value() {return value_;}
    public byte cmp() {return cmp_;}
    public byte ascii() {return ascii_;}
    public char c() {return c_;}

    EnumBP(int value, int cmp, int ascii, char c){
        value_=(byte)value;
        cmp_=(byte)cmp;
        ascii_=(byte)ascii;
        c_=c;
    }
    private final byte value_;
    private final byte cmp_;
    private final byte ascii_;
    private final char c_;

    public static final EnumBP[] cmp2ref_= new EnumBP[256];
    public static final EnumBP[] cmp2read_= new EnumBP[256];

    static {
        Arrays.fill(cmp2ref_,Invalid);
        Arrays.fill(cmp2read_,Invalid);
        for(EnumBP read: EnumSet.allOf(EnumBP.class)){
            if(read.equals(Invalid)) continue;
            final int read_value = ((int)read.cmp()) << 4;
            for(EnumBP ref: EnumSet.allOf(EnumBP.class)){
                if(ref.equals(Invalid)) continue;
                final int key = read_value | (int)ref.cmp();
                cmp2ref_[key] = ref;
                cmp2read_[key] = read;
            }
        }
//    private static final Map<, H5O_type> lookup = new HashMap<Integer, H5O_type>();
//        for (H5O_type s : EnumSet.allOf(H5O_type.class))
//            lookup.put(s.getCode(), s);
    }

    static public String tableToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nR:");
        for (EnumBP entry : EnumBP.cmp2ref_) { sb.append(entry.c()); }
        sb.append("\nR:");
        for (char entry : RefChar) { sb.append(entry); }
        sb.append("\nQ:");
        for (EnumBP entry : EnumBP.cmp2read_) { sb.append(entry.c()); }
        sb.append("\nQ:");
        for (char entry : QueryChar) { sb.append(entry); }
        sb.append("\n");
        return sb.toString();
    }
    private final static char QueryChar[] = {
            ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', //0
            ' ', ' ', ' ', ' ', ' ', ' ', 'A', 'A', 'A', 'A', //10
            'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', // 20
            'A', 'A', 'C', 'C', 'C', 'C', 'C', 'C', 'C', 'C', // 30
            'C', 'C', 'C', 'C', 'C', 'C', 'C', 'C', ' ', ' ', // 40
            ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', // 50
            ' ', ' ', ' ', ' ', 'G', 'G', 'G', 'G', 'G', 'G', // 60
            'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', // 70
            ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', // 80
            ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', // 90
            ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', // 100
            ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', // 110
            ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 'T', 'T', // 120
            'T', 'T', 'T', 'T', 'T', 'T', 'T', 'T', 'T', 'T', // 130
            'T', 'T', 'T', 'T', ' ', ' ', ' ', ' ', ' ', ' ', // 140
            ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', // 150
            ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', // 160
            ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', // 170
            ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', // 180
            ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', // 190
            ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', // 200
            ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', // 210
            ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', // 220
            ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', // 230
            'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', // 240
            'N', 'N', 'N', 'N', 'N', 'N' };

    private final static char RefChar[] = {
            ' ', 'A', 'C', ' ', 'G', ' ', ' ', ' ', 'T', ' ', //0
            ' ', ' ', ' ', ' ', ' ', 'N', ' ', 'A', 'C', ' ', //10
            'G', ' ', ' ', ' ', 'T', ' ', ' ', ' ', ' ', ' ', //20
            ' ', 'N', ' ', 'A', 'C', ' ', 'G', ' ', ' ', ' ', //30
            'T', ' ', ' ', ' ', ' ', ' ', ' ', 'N', ' ', 'A', //40
            'C', ' ', 'G', ' ', ' ', ' ', 'T', ' ', ' ', ' ', //50
            ' ', ' ', ' ', 'N', ' ', 'A', 'C', ' ', 'G', ' ', //60
            ' ', ' ', 'T', ' ', ' ', ' ', ' ', ' ', ' ', 'N', //70
            ' ', 'A', 'C', ' ', 'G', ' ', ' ', ' ', 'T', ' ', //80
            ' ', ' ', ' ', ' ', ' ', 'N', ' ', 'A', 'C', ' ', //90
            'G', ' ', ' ', ' ', 'T', ' ', ' ', ' ', ' ', ' ', //100
            ' ', 'N', ' ', 'A', 'C', ' ', 'G', ' ', ' ', ' ', //110
            'T', ' ', ' ', ' ', ' ', ' ', ' ', 'N', ' ', 'A', //120
            'C', ' ', 'G', ' ', ' ', ' ', 'T', ' ', ' ', ' ', //130
            ' ', ' ', ' ', 'N', ' ', 'A', 'C', ' ', 'G', ' ', //140
            ' ', ' ', 'T', ' ', ' ', ' ', ' ', ' ', ' ', 'N', //150
            ' ', 'A', 'C', ' ', 'G', ' ', ' ', ' ', 'T', ' ', //160
            ' ', ' ', ' ', ' ', ' ', 'N', ' ', 'A', 'C', ' ', //170
            'G', ' ', ' ', ' ', 'T', ' ', ' ', ' ', ' ', ' ', //180
            ' ', 'N', ' ', 'A', 'C', ' ', 'G', ' ', ' ', ' ', //190
            'T', ' ', ' ', ' ', ' ', ' ', ' ', 'N', ' ', 'A', //200
            'C', ' ', 'G', ' ', ' ', ' ', 'T', ' ', ' ', ' ', //210
            ' ', ' ', ' ', 'N', ' ', 'A', 'C', ' ', 'G', ' ', //220
            ' ', ' ', 'T', ' ', ' ', ' ', ' ', ' ', ' ', 'N', //230
            ' ', 'A', 'C', ' ', 'G', ' ', ' ', ' ', 'T', ' ', //240
            ' ', ' ', ' ', ' ', ' ', ' '};

}
