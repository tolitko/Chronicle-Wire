package net.openhft.chronicle.wire;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.NativeBytes;
import org.junit.Test;

import static net.openhft.chronicle.wire.RFCExamplesTest.Fields.*;
import static org.junit.Assert.assertEquals;

/* Based on
https://github.com/OpenHFT/RFC/blob/master/Chronicle/Engine/Remote/Chronicle-Engine-0.1.md
 */
public class RFCExamplesTest {
    /*
    ChronicleMap<Integer, String> map = context.getMap("test", Integer.class, String.class);

    map.put(1, "hello");
    map.put(2, "world");
    map.put(3, "bye");
     */
    @Test
    public void testPuts() {
        Bytes bytes = NativeBytes.nativeBytes();
/*
--- !!meta-data
csp:///service-lookup
tid: 1426502826520
--- !!data
lookup: { relativeUri: test, view: !Map, types: [ !Integer, !String ] }
 */
        Wire text = new TextWire(bytes);
        writeMessageOne(text);
        bytes.flip();
        assertEquals("--- !!meta-data\n" +
                "csp:///service-lookup\n" +
                "tid: 149873598325\n" +
                "--- !!data\n" +
                        "lookup: { relativeUri: test, view: !Map types: [ !Integer !String ] }\n",
                Wires.fromSizePrefixedBlobs(bytes));
        assertEquals("[pos: 0, lim: 117, cap: 1TiB ] " +
                "(٠٠@csp:///service-lookup⒑tid: 149873598325⒑" +
                "E٠٠٠lookup: { relativeUri: test, view: !Map types: [ !Integer !String ] }", bytes.toDebugString());

        Wire bin = new BinaryWire(bytes);
        bytes.clear();
        writeMessageOne(bin);
        bytes.flip();
        assertEquals("[pos: 0, lim: 106, cap: 1TiB ] " +
                "\u001F٠٠@Ãcspñ///service-lookupÃtid\u0090¦\u0094⒒RC" +
                "٠٠٠Ælookup\u00827٠٠٠ËrelativeUriätestÄview¶⒊MapÅtypes\u0082⒘٠٠٠¶⒎Integer¶⒍String", bytes.toDebugString());

        Wire raw = new RawWire(bytes);
        bytes.clear();
        writeMessageOne(raw);
        bytes.flip();
        assertEquals("[pos: 0, lim: 79, cap: 1TiB ] " +
                "\u001A٠٠@⒘///service-lookupu\u009F)å\"٠٠٠-٠٠٠" +
                "⒍lookup\"٠٠٠⒋test⒊Map⒌types⒖٠٠٠⒎Integer⒍String", bytes.toDebugString());
/*
--- !!meta-data
cid: 1
# or
csp://server1/test
tid: 1426502826525
--- !!data
put: [ 1, hello ]
--- !!data
put: [ 2, world ]
--- !!data
put: [ 3, bye ]
*/
        bytes.clear();
        writeMessageTwo(text);
        bytes.flip();
        assertEquals("--- !!meta-data\n" +
                "\n" +
                "csp://server1/test\n" +
                "cid: 1\n" +
                "--- !!data\n" +
                "put: [ 1, hello ]\n" +
                "--- !!data\n" +
                "put: [ 2, world ]\n" +
                "--- !!data\n" +
                        "put: [ 3, bye ]\n",
                Wires.fromSizePrefixedBlobs(bytes));
        assertEquals("[pos: 0, lim: 92, cap: 1TiB ] " +
                "\u001B٠٠@⒑csp://server1/test⒑cid: 1⒑" +
                "⒘٠٠٠put: [ 1, hello ]" +
                "⒘٠٠٠put: [ 2, world ]" +
                "⒖٠٠٠put: [ 3, bye ]", bytes.toDebugString());

        bytes.clear();
        writeMessageTwo(bin);
        bytes.flip();
        assertEquals("[pos: 0, lim: 86, cap: 1TiB ] " +
                "\u0018٠٠@Ãcspî//server1/testÃcid⒈" +
                "⒗٠٠٠Ãput\u0082⒎٠٠٠⒈åhello" +
                "⒗٠٠٠Ãput\u0082⒎٠٠٠⒉åworld" +
                "⒕٠٠٠Ãput\u0082⒌٠٠٠⒊ãbye", bytes.toDebugString());

        bytes.clear();
        writeMessageTwo(raw);
        bytes.flip();
        assertEquals("[pos: 0, lim: 103, cap: 1TiB ] " +
                "\u0017٠٠@⒕//server1/test⒈٠٠٠٠٠٠٠\u0016" +
                "٠٠٠⒊put⒕٠٠٠⒈٠٠٠٠٠٠٠⒌hello\u0016" +
                "٠٠٠⒊put⒕٠٠٠⒉٠٠٠٠٠٠٠⒌world⒛" +
                "٠٠٠⒊put⒓٠٠٠⒊٠٠٠٠٠٠٠⒊bye", bytes.toDebugString());
    }

    enum Fields implements WireKey {
        csp, tid, lookup, relativeUri, view, types
    }

    public void writeMessageOne(Wire wire) {
        wire.writeDocument(true, out ->
                out.write(csp).text("///service-lookup")
                        .write(tid).int64(149873598325L));
        wire.writeDocument(false, out ->
                out.write(lookup).marshallable(out2 ->
                        out2.write(relativeUri).text("test")
                                .write(view).type("Map")
                                .write(types).sequence(vo -> {
                            vo.type("Integer");
                            vo.type("String");
                        })));
    }

    private void writeMessageTwo(Wire wire) {
        wire.writeDocument(true, out ->
                out.write(() -> "csp").text("//server1/test")
                        .write(() -> "cid").int64(1));
        String[] words = ",hello,world,bye".split(",");
        for (int i = 1; i < words.length; i++) {
            int n = i;
            wire.writeDocument(false, out ->
                    out.write(() -> "put").sequence(vo -> {
                        vo.int64(n);
                        vo.text(words[n]);
                    }));
        }

    }
}