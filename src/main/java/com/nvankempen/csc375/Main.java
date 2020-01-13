package com.nvankempen.csc375;

import com.nvankempen.csc375.gui.GUI;
import com.nvankempen.csc375.sockets.MainSocket;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;

public final class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length == 0) {
            final MainSocket socket = new MainSocket();
            final Scanner sc = new Scanner(System.in);
            while (sc.hasNextLine()) {
                final String[] peer = sc.nextLine().split(" ");
                if (peer.length != 2) continue;
                socket.sendHandshake(InetAddress.getByName(peer[0]), Integer.parseInt(peer[1]));
            }
        } else new GUI("Sudoku", args);
    }
}
