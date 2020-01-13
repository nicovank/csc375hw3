package com.nvankempen.csc375.sockets;


import com.nvankempen.csc375.Sudoku;
import com.nvankempen.csc375.SudokuSolver;
import com.nvankempen.underscore.utils.tuples.Doublet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.nvankempen.csc375.Constants.MAX_PACKET_SIZE;

public final class MainSocket {

    private static final int PORT = 13126;

    // OPCODES
    private static final byte HANDSHAKE = 13;
    private static final byte FAIL = 17;
    private static final byte SUCCESS = 31;
    private static final byte SUDOKU = 71;

    private final Queue<Doublet<InetAddress, Integer>> helpers = new ConcurrentLinkedQueue<>();
    private final Map<Doublet<InetAddress, Integer>, SudokuSolver> tasks = new ConcurrentHashMap<>();
    private final DatagramSocket socket;

    public MainSocket() throws SocketException {
        socket = new DatagramSocket(PORT);
        socket.setSoTimeout(50);
        System.out.println("Listening for connections on port " + PORT + ".");

        new Thread(() -> {
            while (true) {
                final DatagramPacket packet = new DatagramPacket(new byte[MAX_PACKET_SIZE], MAX_PACKET_SIZE);

                try {
                    socket.receive(packet);
                    new Thread(() -> {
                        try {
                            handle(packet);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
                } catch (SocketTimeoutException ignored) {

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public boolean tryAskingForHelp(SudokuSolver task) {
        final Doublet<InetAddress, Integer> peer = helpers.poll();

        if (peer != null) {
            System.out.println("Sending work to " + peer.getA());
            tasks.put(peer, task);

            try {
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                out.write(SUDOKU);
                task.getSudoku().serialize(out);

                socket.send(new DatagramPacket(out.toByteArray(), out.size(), peer.getA(), peer.getB()));
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        } else return false;
    }

    private void handle(DatagramPacket packet) throws IOException {
        if (packet.getData().length == 0) return;

        final InetAddress address = packet.getAddress();
        final int port = packet.getPort();

        final ByteBuffer buffer = ByteBuffer.wrap(packet.getData(), packet.getOffset(), packet.getLength());
        final byte code = buffer.get();

        if (code == HANDSHAKE) {
            final Doublet<InetAddress, Integer> peer = new Doublet<>(address, port);
            if (!helpers.contains(peer)) {
                System.out.println("Contacted by helper: " + address);
                helpers.add(peer);
                sendHandshake(address, port);
            }
        } else if (code == FAIL) {
            System.out.println("Received fail from: " + address);
            tasks.get(new Doublet<>(address, port)).markAsCompleted(null);
            helpers.add(new Doublet<>(address, port));
        } else if (code == SUCCESS) {
            System.out.println("Received success from: " + address);
            tasks.get(new Doublet<>(address, port)).markAsCompleted(Sudoku.deserialize(buffer));
            helpers.add(new Doublet<>(address, port));
        } else if (code == SUDOKU) {
            System.out.println("Received work from: " + address);
            final SudokuSolver solver = new SudokuSolver(Sudoku.deserialize(buffer), this);
            final Sudoku solution = solver.invoke();

            if (solution == null) socket.send(new DatagramPacket(new byte[]{FAIL}, 1, address, port));
            else {
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                out.write(SUCCESS);
                solution.serialize(out);
                socket.send(new DatagramPacket(out.toByteArray(), out.size(), address, port));
            }
        } else System.out.println("Unknown code: " + code);
    }

    public void sendHandshake(InetAddress address, int port) throws IOException {
        System.out.println("Sending handshake to " + address + "...");
        socket.send(new DatagramPacket(new byte[]{HANDSHAKE}, 1, address, port));
    }
}
