package org.firstinspires.ftc.teamcode.utilities;

import android.os.Environment;

import androidx.annotation.NonNull;

import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

public class WriteObservations {
    private double calculatedDistance;
    private double targetX;
    private double targetY;
    private YawPitchRollAngles orientation;
    private double targetArea;
    private int fiducialResults;
    private int fiducialID;

    private double fiducialTx;
    private Pose3D fiducialTargetPose;

    private final static String filePath = "observations.csv";
    private String data;
    public WriteObservations(double calculatedDistance, double targetX, double targetY, YawPitchRollAngles orientation, double targetArea, int fiducialResults, int fiducialID, double fiducialTx, Pose3D fiducialTargetPose){
        this.calculatedDistance = calculatedDistance;
        this.targetX = targetX;
        this.targetY = targetY;
        this.orientation = orientation;
        this.targetArea = targetArea;
        this.fiducialResults = fiducialResults;
        this.fiducialID = fiducialID;
        this.fiducialTx = fiducialTx;
        this.fiducialTargetPose = fiducialTargetPose;
        data = toString();
    }
    public void writeToFile(String filePath, String data){
        saveRecord(data, filePath);
    }
    public String getData(){
        return data;
    }
    public static String getfilePath(){
        return filePath;
    }
    private static void saveRecord(String data, String filePath){
        try{
            final File teamDir = new File(Environment.getExternalStorageDirectory(), filePath);
            FileWriter fw = new FileWriter(teamDir, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);

            pw.println(data);
            pw.flush();
            pw.close();
            System.out.println("Wrote to file.");
        }catch(Exception e){
            System.out.println("Failed to write.");
        }
    }
    public static void writeHeader(){
        String header = "calculatedDistance, targetX, targetY, robot yaw, robot pitch, robot roll, targetArea, fiducialResults, fiducialID, fiducialTx, x, y, z, unit, AprilTag yaw, AprilTag pitch, AprilTag roll";
        saveRecord(header, filePath);
    }
    @NonNull
    public String toString(){
        String cd = ""+calculatedDistance;
        String tX = ""+targetX;
        String tY = ""+targetY;
        String oY = ""+orientation.getYaw();
        String oP = ""+orientation.getPitch();
        String oR = ""+orientation.getRoll();
        String tA = ""+targetArea;
        String fR = ""+fiducialResults;
        String fID = ""+fiducialID;
        String fT = ""+fiducialTx;
        //String fTP = ""+fiducialTargetPose;
        String fTPX = ""+fiducialTargetPose.getPosition().x;
        String fTPY = ""+fiducialTargetPose.getPosition().y;
        String fTPZ = ""+fiducialTargetPose.getPosition().z;
        String fTPU = ""+fiducialTargetPose.getPosition().unit;
        String fTPYa = ""+fiducialTargetPose.getOrientation().getYaw();
        String fTPP = ""+fiducialTargetPose.getOrientation().getPitch();
        String fTPR = ""+fiducialTargetPose.getOrientation().getRoll();
        return cd+", "+tX+", "+tY+", "+oY+", "+oP+", "+oR+", "+tA+", "+fR+", "+fID+", "+fT+","+fTPX+","+fTPY+","+fTPZ+","+fTPU+","+fTPYa+","+fTPP+","+fTPR;
    }
}
