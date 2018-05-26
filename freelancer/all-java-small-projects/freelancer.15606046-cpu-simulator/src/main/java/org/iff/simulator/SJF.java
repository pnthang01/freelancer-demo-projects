package org.iff.simulator;

/**
 * Created by thangpham on 12/11/2017.
 */

import java.util.Scanner;

class SJF {
    public static void main(String args[]) {
        Scanner sc = new Scanner(System.in);
        int n, BT[], WT[], TAT[];
        System.out.println("Enter no of process");
        n = sc.nextInt();
        BT = new int[n + 1];
        WT = new int[n + 1];
        TAT = new int[n + 1];
        float AWT = 0;
        System.out.println("Enter Burst time for each process");
        for (int i = 0; i < n; i++) {
            System.out.println("Enter BT for process " + (i + 1));
            BT[i] = sc.nextInt();
        }
        for (int i = 0; i < n; i++) {
            WT[i] = 0;
            TAT[i] = 0;
        }
        int temp;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n - 1; j++) {
                if (BT[j] > BT[j + 1]) {
                    temp = BT[j];
                    BT[j] = BT[j + 1];
                    BT[j + 1] = temp;
                    temp = WT[j];
                    WT[j] = WT[j + 1];
                    WT[j + 1] = temp;
                }
            }
        }
        for (int i = 0; i < n; i++) {
            TAT[i] = BT[i] + WT[i];
            WT[i + 1] = TAT[i];
        }
        TAT[n] = WT[n] + BT[n];
        System.out.println("PROCESS\tBT\tWT\tTAT");
        for (int i = 0; i < n; i++)
            System.out.println((i + 1) + "\t" + BT[i] + "\t" + WT[i] + "\t" + TAT[i]);
        for (int j = 0; j < n; j++) AWT += WT[j];
        AWT = AWT / n;
        System.out.println(" ***********************************************");
        System.out.println("Avg waiting time =" + AWT + "***********************************************");
    }
}
