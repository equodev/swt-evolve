package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Port of upstream Eclipse SWT Snippet79 (define my own data transfer type), validating
 * that a user-defined ByteArrayTransfer subclass exercises real javaToNative/nativeToJava
 * serialization on the Dart-backed client (see DartByteArrayTransfer's in-process byte[]
 * round trip, added alongside issue #755's generalized DND) instead of the previous no-op
 * stubs, plus DND.DROP_DEFAULT resolution in dragEnter/dragOperationChanged.
 */
public class Snippet79DragDropSnippet {

    static class MyType {
        String fileName;
        long fileLength;
        long lastModified;
    }

    static class MyTransfer extends ByteArrayTransfer {

        private static final String MYTYPENAME = "name_for_my_type";
        private static final int MYTYPEID = registerType(MYTYPENAME);
        private static MyTransfer _instance = new MyTransfer();

        public static MyTransfer getInstance() {
            return _instance;
        }

        @Override
        public void javaToNative(Object object, TransferData transferData) {
            if (!checkMyType(object) || !isSupportedType(transferData)) {
                DND.error(DND.ERROR_INVALID_DATA);
            }
            MyType[] myTypes = (MyType[]) object;
            try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                    DataOutputStream writeOut = new DataOutputStream(out)) {
                for (MyType myType : myTypes) {
                    byte[] buffer = myType.fileName.getBytes();
                    writeOut.writeInt(buffer.length);
                    writeOut.write(buffer);
                    writeOut.writeLong(myType.fileLength);
                    writeOut.writeLong(myType.lastModified);
                }
                byte[] buffer = out.toByteArray();
                super.javaToNative(buffer, transferData);
            } catch (IOException e) {
            }
        }

        @Override
        public Object nativeToJava(TransferData transferData) {
            if (!isSupportedType(transferData))
                return null;
            byte[] buffer = (byte[]) super.nativeToJava(transferData);
            if (buffer == null)
                return null;
            MyType[] myData = new MyType[0];
            try (ByteArrayInputStream in = new ByteArrayInputStream(buffer);
                    DataInputStream readIn = new DataInputStream(in)) {
                while (readIn.available() > 20) {
                    MyType datum = new MyType();
                    int size = readIn.readInt();
                    byte[] name = new byte[size];
                    readIn.read(name);
                    datum.fileName = new String(name);
                    datum.fileLength = readIn.readLong();
                    datum.lastModified = readIn.readLong();
                    MyType[] newMyData = new MyType[myData.length + 1];
                    System.arraycopy(myData, 0, newMyData, 0, myData.length);
                    newMyData[myData.length] = datum;
                    myData = newMyData;
                }
            } catch (IOException ex) {
                return null;
            }
            return myData;
        }

        @Override
        protected String[] getTypeNames() {
            return new String[] { MYTYPENAME };
        }

        @Override
        protected int[] getTypeIds() {
            return new int[] { MYTYPEID };
        }

        boolean checkMyType(Object object) {
            if (object == null || !(object instanceof MyType[]) || ((MyType[]) object).length == 0) {
                return false;
            }
            for (MyType myType : (MyType[]) object) {
                if (myType == null || myType.fileName == null || myType.fileName.length() == 0) {
                    return false;
                }
            }
            return true;
        }

        @Override
        protected boolean validate(Object object) {
            return checkMyType(object);
        }
    }

    public static void main(String[] args) {
        Config.useEquo(Label.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Snippet 79");
        shell.setLayout(new FillLayout());
        final Label label1 = new Label(shell, SWT.BORDER | SWT.WRAP);
        label1.setText("Drag Source for MyData[]");
        final Label label2 = new Label(shell, SWT.BORDER | SWT.WRAP);
        label2.setText("Drop Target for MyData[]");

        DragSource source = new DragSource(label1, DND.DROP_COPY);
        source.setTransfer(MyTransfer.getInstance());
        source.addDragListener(new DragSourceAdapter() {
            @Override
            public void dragSetData(DragSourceEvent event) {
                MyType myType1 = new MyType();
                myType1.fileName = "C:\\abc.txt";
                myType1.fileLength = 1000;
                myType1.lastModified = 12312313;
                MyType myType2 = new MyType();
                myType2.fileName = "C:\\xyz.txt";
                myType2.fileLength = 500;
                myType2.lastModified = 12312323;
                event.data = new MyType[] { myType1, myType2 };
            }
        });
        DropTarget target = new DropTarget(label2, DND.DROP_COPY | DND.DROP_DEFAULT);
        target.setTransfer(MyTransfer.getInstance());
        target.addDropListener(new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetEvent event) {
                if (event.detail == DND.DROP_DEFAULT) {
                    event.detail = DND.DROP_COPY;
                }
            }

            @Override
            public void dragOperationChanged(DropTargetEvent event) {
                if (event.detail == DND.DROP_DEFAULT) {
                    event.detail = DND.DROP_COPY;
                }
            }

            @Override
            public void drop(DropTargetEvent event) {
                if (event.data != null) {
                    MyType[] myTypes = (MyType[]) event.data;
                    if (myTypes != null) {
                        String string = "";
                        for (MyType myType : myTypes) {
                            string += myType.fileName + " ";
                        }
                        label2.setText(string);
                    }
                }
            }
        });
        shell.setSize(200, 200);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }
}
