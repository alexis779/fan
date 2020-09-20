package tech.vineyard.parser;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Scanner;

public class SameOccurrenceApplication {
    private static final Logger LOG = LogManager.getLogger(SameOccurrenceApplication.class);

    public static void main(String[] args) throws Exception {
        assert args.length == 1;
        new SameOccurrenceApplication(args[0]);
    }

    private ByteBuffer byteBuffer;
    private String input;

    /**
     *
     * @param path path to input file to parse
     * @throws IOException
     */
    public SameOccurrenceApplication(String path) throws IOException {
        byteBuffer = readFile(path);
        //byteBuffer = mmapFile(path);
        input = decodeArray(byteBuffer);
        //input = decodeBuffer(byteBuffer);
        //antlr();
        split();
    }

    public void antlr() {
        long start = System.currentTimeMillis();
        CharStream charStream = CharStreams.fromString(input);
        //System.out.println("Input: " + charStream);

        Lexer lexer = new SameOccurrenceLexer(charStream);
        CommonTokenStream stream = new CommonTokenStream(lexer);
        SameOccurrenceParser parser = new SameOccurrenceParser(stream);
        parser.addParseListener(new SameOccurrenceParseListener());
        //parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
        parser.r();
        long end = System.currentTimeMillis();
        LOG.info("Antlr in {} ms", end - start);
    }


    public void scan() {
        long start = System.currentTimeMillis();
        Scanner scanner = new Scanner(input);
        int n = scanner.nextInt();
        int q = scanner.nextInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = scanner.nextInt();
        }
        for (int t = 0; t < q; t++) {
            int x = scanner.nextInt();
            int y = scanner.nextInt();
        }
        scanner.close();
        long end = System.currentTimeMillis();
        LOG.info("Scanning in {} ms", end - start);
    }


    int tokenOffset = 0;
    public void split() {
        long end;
        long start = System.currentTimeMillis();
        String[] tokens = input.split("\\s+");
        end = System.currentTimeMillis();
        LOG.info(" String.split in {} ms", end - start);
        long startParseInt = System.currentTimeMillis();
        int n = parseToken(tokens);
        int q = parseToken(tokens);
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = parseToken(tokens);
        }
        for (int t = 0; t < q; t++) {
            int x = parseToken(tokens);
            int y = parseToken(tokens);
        }
        assert tokenOffset == tokens.length;
        end = System.currentTimeMillis();
        LOG.info(" int parsing in {} ms", end - startParseInt);
        LOG.info("Splitting in {} ms", end - start);
    }

    private int parseToken(String[] tokens) {
        return Integer.parseInt(tokens[tokenOffset++]);
        // return Integer.valueOf(tokens[tokenOffset++]);
    }


    int byteOffset = 0;
    public void raw() {
        long start = System.currentTimeMillis();
        int n = parseInt();
        int q = parseInt();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = parseInt();
        }
        for (int t = 0; t < q; t++) {
            int x = parseInt();
            int y = parseInt();
        }
        long end = System.currentTimeMillis();
        LOG.info("Raw parsing in {} ms", end - start);
    }

    private static final int BASE = 10;
    private static final char ZERO = '0';
    private int parseInt() {
        int start = byteOffset;
        int end = getTokenEnd();

        int i = 0;
        int pow = 1;
        for (int pos = end; pos >= start; pos--) {
            int digit = byteBuffer.get(pos) - ZERO;
            i += pow * digit;
            pow *= BASE;
        }
        return i;
    }

    private int getTokenEnd() {
        while (isWhitespace(byteBuffer.get(byteOffset))) {
            byteOffset++;
        }
        int pos = byteOffset;
        while(! isWhitespace(byteBuffer.get(pos))) {
            pos++;
        }
        byteOffset = pos;
        return pos-1;
    }

    private static final char LF = '\n';
    private static final char CR = '\r';
    private static final char SPACE = ' ';
    private boolean isWhitespace(byte b) {
        return b == LF || b == CR || b == SPACE;
    }


    private ByteBuffer readFile(String path) throws IOException {
        long start = System.currentTimeMillis();
        FileChannel channel = new FileInputStream(getFile(path)).getChannel();

        int size = (int) channel.size();
        int log2 = (int) Math.ceil(Math.log(size) / Math.log(2));
        int bufferSize = 1 << log2;

        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        channel.read(buffer);
        buffer.flip();
        long end = System.currentTimeMillis();

        LOG.info("Reading in {} ms", (end - start));

        return buffer;
    }

    private ByteBuffer mmapFile(String path) throws IOException {
        long start = System.currentTimeMillis();
        FileChannel channel = new RandomAccessFile(getFile(path), "r").getChannel();

        MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        buffer.load();
        long end = System.currentTimeMillis();

        LOG.info("Mmap in {} ms", (end - start));

        return buffer;
    }

    private String getFile(String path) {
        return path;
    }


    private static final String UTF8 = "UTF-8";
    private String decodeArray(ByteBuffer byteBuffer) {
        long start = System.currentTimeMillis();
        String content = new String(byteBuffer.array(), 0, byteBuffer.limit(), Charset.forName(UTF8));
        long end = System.currentTimeMillis();
        LOG.info("Decoding in {} ms", (end - start));
        return content;
    }

    private String decodeBuffer(ByteBuffer byteBuffer) throws CharacterCodingException {
        long start = System.currentTimeMillis();
        CharsetDecoder decoder = Charset.forName(UTF8).newDecoder();
        CharBuffer decoded = decoder.decode(byteBuffer);
        long end = System.currentTimeMillis();
        LOG.info("Decoding buffer in {} ms", (end - start));
        return decoded.toString();
    }
}
