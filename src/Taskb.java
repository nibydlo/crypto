import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Taskb {

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {

        HashMap<Long, String> inputBlocks = new HashMap<>();
        HashMap<Long, byte[]> mercleTree = new HashMap<>();
        ArrayDeque<Long> queue = new ArrayDeque<>();
        ArrayList<Long> queueAl = new ArrayList<>();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

        // input
        int h = Integer.parseInt(bufferedReader.readLine());
        long n = Long.parseLong(bufferedReader.readLine());

        for (int i = 0; i < n; i++) {
            String[] idAndOther = bufferedReader.readLine().split(" ");
            inputBlocks.put(Long.parseLong(idAndOther[0]), idAndOther[1]);
        }

        long q = Long.parseLong(bufferedReader.readLine());
        String[] qStringArray = bufferedReader.readLine().split(" ");

        // build tree (id = idInput + pow(2, h) - 1)
        long shift = (long) Math.pow(2, h) - 1;
        for (long e : inputBlocks.keySet()) {
            // concat 0 and decoded data and encode it in sha-256
            byte[] dataIn64Byte = Base64.getDecoder().decode(inputBlocks.get(e));
            byte[] toMd = new byte[dataIn64Byte.length + 1];
            toMd[0] = 0;
            for (int j = 0; j < dataIn64Byte.length; j++) {
                toMd[1 + j] = dataIn64Byte[j];
            }
            messageDigest.update(toMd);
            mercleTree.put(e + shift, messageDigest.digest());
            //queue.add(e + shift);
            queueAl.add(e + shift);
        }

        queueAl.sort(Long::compareTo);
        for (Long e : queueAl) {
            queue.add(e);
        }

        while (!queue.isEmpty()) {
            Long curElem = queue.poll();
            //System.out.println("curElem = " + curElem);
            byte[] dig1 = null, dig2 = null;
            if (curElem % 2 == 1 && queue.contains(curElem + 1)) {
                Long nextElem = queue.poll();

              //  System.out.println("nextElem = " + nextElem);
                dig1 = mercleTree.get(curElem);
                dig2 = mercleTree.get(nextElem);

            } else {
                if (curElem % 2 == 1) {
                    dig1 = mercleTree.get(curElem);
                } else {
                    dig2 = mercleTree.get(curElem);
                }
            }

            Long parent = (curElem - 1) / 2;

            //System.out.println("parent = " + parent);
            int dig1Length = dig1 == null ? 0 : dig1.length;
            int dig2Length = dig2 == null ? 0 : dig2.length;
            //System.out.println("lengths: " + dig1Length + " " + dig2Length);
            byte[] dig = new byte[dig1Length + dig2Length + 2];
            dig[0] = 1;
            for (int k = 0; k < dig1Length; k++) {
                dig[1 + k] = dig1[k];
            }
            dig[dig1Length + 1] = 2;
            for (int k = 0; k < dig2Length; k++) {
                dig[1 + dig1Length + 1 + k] = dig2[k];
            }
            messageDigest.update(dig);
            mercleTree.put(parent, messageDigest.digest());

            if (parent != 0) {
                queue.add(parent);
            }
        }

        // construct proofs
        for (int i = 0; i < q; i++) {
            Long id = Long.parseLong(qStringArray[i]);
            System.out.println(id + " " + inputBlocks.getOrDefault(id, "null"));
            id += shift;
            for (int j = 0; j < h; j++) {

                long neighbour = (id % 2 == 1) ? (id + 1) : (id - 1);
                //System.out.println("nei = " + neighbour);
                if (mercleTree.containsKey(neighbour)) {
                    System.out.println(new String(Base64.getEncoder().encode(mercleTree.get(neighbour))));
                } else {
                    System.out.println("null");
                }
                id = (id - 1) / 2;
            }
        }
    }
}
