import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static java.lang.Math.pow;

public class TaskC {

    static int H = 8, blockSize = 32;

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {

        TaskC taskC = new TaskC();
        taskC.run();
    }

    private static ArrayList<Integer> getBin(int id, int l) {

        ArrayList<Integer> res = new ArrayList<>();
        for (int i = 0; i < l; i++) {
            res.add((id / (int) pow(2, i)) % 2);
        }
        return res;
    }

    private static ArrayList<byte[]> decomposeX(byte[] X) {
        ArrayList<byte[]> Xlist = new ArrayList<>();
        for (int i = 0; i < 256; i++) {
            byte[] temp = new byte[blockSize];
            System.arraycopy(X, i * blockSize, temp, 0, blockSize);
            Xlist.add(temp);
        }
        return Xlist;
    }

    private static ArrayList<ArrayList<byte[]>> decomposeY(byte[] Y) {
        ArrayList<ArrayList<byte[]>> Ylist = new ArrayList<>();
        for (int i = 0; i < 512; i++) {
            byte[] temp = new byte[blockSize];
            System.arraycopy(Y, i * blockSize, temp, 0, blockSize);
            if (i < 256) {
                Ylist.add(new ArrayList<>());
            }
            Ylist.get(i % 256).add(temp);
        }
        return Ylist;
    }

    private static int checkDisposable(String doc, byte[] X, byte[] Y) throws NoSuchAlgorithmException {

        ArrayList<byte[]> Xlist = decomposeX(X);
        ArrayList<ArrayList<byte[]>> Ylist = decomposeY(Y);
        int mistakeCount = 0;
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        for (int i = 0; i < 256; i++) {
            byte[] Xhashed = md.digest(Xlist.get(i));
            int bit = Character.getNumericValue(doc.charAt(i));

            if (!Arrays.equals(Xhashed, Ylist.get(i).get(bit))) {
                mistakeCount++;
            }
        }
        return mistakeCount;
    }

    private static boolean checkMerkle(byte[] rootHashByteArray, ArrayList<byte[]> mercleProof, byte[] X, int id) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");

        // decode and hash value of leaf
        byte[] digest = new byte[X.length + 1];
        digest[0] = 0;
        System.arraycopy(X, 0, digest, 1, X.length);
        md.update(digest);
        digest = md.digest();

        ArrayList<Integer> turns = getBin(id, H);
        for (int j = 0; j < H; j++) {
            byte[] dig1, dig2;
            if (turns.get(j) == 0) { // we are on the left
                dig1 = digest;
                dig2 = mercleProof.get(j);
            } else { // we are on the righ
                dig1 = mercleProof.get(j);
                dig2 = digest;
            }

            byte[] dig = new byte[dig1.length + dig2.length + 2];
            dig[0] = 1;
            System.arraycopy(dig1, 0, dig, 1, dig1.length);
            dig[dig1.length + 1] = 2;
            System.arraycopy(dig2, 0, dig, dig1.length + 2, dig2.length);
            md.update(dig);
            digest = md.digest();
        }

        return Arrays.equals(digest, rootHashByteArray);
    }

    private static String sign(ArrayList<ArrayList<byte[]>> key, String doc) {

        //ArrayList<byte[]> signList = new ArrayList<>();
        byte[] signByteArray = new byte[256 * blockSize];
        for (int i = 0; i < 256; i++) {
            int bit = Character.getNumericValue(doc.charAt(i));
            byte[] temp = key.get(i).get(bit);
            System.arraycopy(temp, 0, signByteArray, i * blockSize, blockSize);
        }

        return Base64.getEncoder().encodeToString(signByteArray);
    }

    private void run() throws NoSuchAlgorithmException, IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        ArrayList<Integer> readiness = new ArrayList<>();
        for (int i = 0; i < 256; i++) {
            readiness.add(0);
        }

        // read root
        byte[] mercleRoot = Base64.getDecoder().decode(reader.readLine());

        String keyNumberString;
        String zeroString = String.join("", Collections.nCopies(256, "0"));
        String oneString = String.join("", Collections.nCopies(256, "1"));
        byte[] dummyAr = {};
        ArrayList<ArrayList<ArrayList<byte[]>>> keyArray = new ArrayList<>();
        for (int i = 0; i < Math.pow(2, H); i++) {
            keyArray.add(new ArrayList<>());
            for (int j = 0; j < Math.pow(2, H); j++) {
                keyArray.get(i).add(new ArrayList<>());
                keyArray.get(i).get(j).add(dummyAr);
                keyArray.get(i).get(j).add(dummyAr);
            }
        }

        while ((keyNumberString = reader.readLine()) != null) {

            int keyNumber = Integer.parseInt(keyNumberString);
            String docOut = readiness.get(keyNumber) == 0 ? zeroString : oneString;
            System.out.println(docOut);

            // input public and secret disponsable keys
            byte[] X = Base64.getDecoder().decode(reader.readLine());
            byte[] Y = Base64.getDecoder().decode(reader.readLine());

            // check disponsable sign
            int firstMistakes = checkDisposable(docOut, X, Y);

            // input mercle proof
            ArrayList<byte[]> mercleProof = new ArrayList<>();
            for (int i = 0; i < H; i++) {
                mercleProof.add(Base64.getDecoder().decode(reader.readLine()));
            }

            // check Mercle proof
            boolean secondCheck = checkMerkle(mercleRoot, mercleProof, Y, keyNumber);

            // input document to sign
            String docIn = reader.readLine();

            // check if sign is right
            if (firstMistakes > 0 || !secondCheck) {
                System.out.println("NO");
            } else {
                System.out.println("YES");
                // if it is right maybe we should update our keys
                if (readiness.get(keyNumber) != 2) {
                    ArrayList<byte[]> Xlist = decomposeX(X);
                    for (int i = 0; i < 256; i++) {
                        keyArray.get(keyNumber).get(i).set(readiness.get(keyNumber), Xlist.get(i));
                    }
                    readiness.set(keyNumber, readiness.get(keyNumber) + 1);
                }
            }

            if (readiness.get(keyNumber) != 2) {
                if (readiness.get(keyNumber) == 1 && docIn.equals(zeroString)) {
                    System.out.println("YES");
                    System.out.println(sign(keyArray.get(keyNumber), docIn));
                } else {
                    System.out.println("NO");
                }
            } else {
                System.out.println("YES");
                System.out.println(sign(keyArray.get(keyNumber), docIn));
                return;
            }
        }
    }
}
