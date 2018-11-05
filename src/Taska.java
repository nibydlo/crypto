import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

import static java.lang.Math.pow;

public class Taska {

    private static ArrayList<Integer> getBin(int id, int l) {

        ArrayList<Integer> res = new ArrayList<>();
        for (int i = 0; i < l; i++) {
            res.add((id / (int) pow(2, i)) % 2);
        }
        return res;
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        // read height
        int h = Integer.parseInt(reader.readLine());

        // read & decode rootHash
        String rootHashString = reader.readLine();
        byte[] rootHashByteArray = Base64.getDecoder().decode(rootHashString);

        // read number of questions
        int q = Integer.parseInt(reader.readLine());

        for (int i = 0; i < q; i++) {

            // work with question
            String[] idAndOther = reader.readLine().split(" ");
            int id = Integer.parseInt(idAndOther[0]);
            String dataIn64 = idAndOther[1];

            // decode and hash value of leaf
            byte[] digest;
            if (dataIn64.equals("null")) {
                md.update(Base64.getDecoder().decode(""));
                digest = null;
            } else {
                byte[] dataIn64Byte = Base64.getDecoder().decode(dataIn64);
                byte[] toMd = new byte[dataIn64Byte.length + 1];
                toMd[0] = 0;
                for (int j = 0; j < dataIn64Byte.length; j++) {
                    toMd[1 + j] = dataIn64Byte[j];
                }
                md.update(toMd);
                digest = md.digest();
            }

            ArrayList<Integer> turns = getBin(id, h);
            //System.out.println(turns);
            for (int j = 0; j < h; j++) {
                byte[] dig1, dig2;
                if (turns.get(j) == 0) { // we are on the left
                    dig1 = digest;
                    String in = reader.readLine();
                    if (in.equals("null")) {
                        dig2 = null;
                    } else {
                        dig2 = Base64.getDecoder().decode(in);
                    }
                } else { // we are on the right
                    String in = reader.readLine();
                    if (in.equals("null")) {
                        dig1 = null;
                    } else {
                        dig1 = Base64.getDecoder().decode(in);
                    }
                    dig2 = digest;
                }

                if (dig1 == null && dig2 == null) {
                    digest = null;
                } else {
                    int dig1Length = dig1 == null ? 0 : dig1.length;
                    int dig2Length = dig2 == null ? 0 : dig2.length;
                    byte[] dig = new byte[dig1Length + dig2Length + 2];
                    dig[0] = 1;
                    for (int k = 0; k < dig1Length; k++) {
                        dig[1 + k] = dig1[k];
                    }
                    dig[dig1Length + 1] = 2;
                    for (int k = 0; k < dig2Length; k++) {
                        dig[1 + dig1Length + 1 + k] = dig2[k];
                    }
                    md.update(dig);
                    digest = md.digest();
                }
            }

            if (Arrays.equals(digest, rootHashByteArray)) {
                System.out.println("YES");
            } else {
                System.out.println("NO");
            }
        }
    }
}
