package Schedules;

import Calculations.CalculatePath;
import DataBase.DB;
import DroneDeliveryPakage.Distributor;
import DroneDeliveryPakage.Order;
import DronePakage.DroneV1;
import DronePakage.OpenWindow;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by georgipavlov on 26.02.16.
 */
public class SchedulesForDeliveries {
    private List<DroneV1> drones;
    private Order order;
    ArrayList<OpenWindowsWithDroneId> potentionalOpenWindows;
    Distributor distributor;

    public void starSearch(Order order, int countDrones, Distributor distributor){
        drones = distributor.getDrones();
        this.order = order;
        tryToPutInOpenWindows(countDrones);
        this.distributor = distributor;


    }

    public void tryToPutInOpenWindows(int countDrones){
        potentionalOpenWindows = new ArrayList<>();
        double tempStartTime;
        double tempEndTime;
        for (int j = 0; j < drones.size() ; j++) {
            for (int i = 0; i <drones.get(j).openWindows.size() ; i++) {
                tempStartTime = drones.get(j).openWindows.get(i).getStartTime();
                tempEndTime = drones.get(j).openWindows.get(i).getCurentTime();
                OpenWindowsWithDroneId openWinId =new OpenWindowsWithDroneId(drones.get(j).openWindows.get(i),j);
                potentionalOpenWindows.add(openWinId);

              boolean candidate = isPotentialCandidate(tempStartTime,tempEndTime, drones.get(j).openWindows.get(i));
                if(!candidate){
                   potentionalOpenWindows.remove(openWinId);
                    continue;
                }
                if(countDrones > 1){
                   startProceduresForAdding(j,tempStartTime,tempEndTime,countDrones,openWinId);
                }else {
                    //startPuting
                }

            }
        }
    }

    public boolean isPotentialCandidate(double tempStartTime,double tempEndTime,OpenWindow openWindow){
        if(willHaveЕnoughBattery(tempStartTime,tempEndTime,openWindow)){
            return true;
        }
     return false;
    }

    public boolean isInTime(double tempStartTime,double tempEndTime){

        if(tempEndTime - tempStartTime >= order.getTime() ){
            return true;
        }
        return false;
    }

    public boolean willHaveЕnoughBattery(double tempStartTime,double tempEndTime,OpenWindow openWindow){
        CalculatePath path = new CalculatePath();
        int requiredBattery =0;
        int pathCalc = path.calculatePath(order.getCoordinates(), DB.warehouses.get(0));
        if(openWindow.getBateryOnStart() < pathCalc){
            requiredBattery = pathCalc - openWindow.getBateryOnStart();
            tempStartTime = getNewStartTime(requiredBattery);
        }
        if(isInTime(tempStartTime,tempEndTime)){
            if(tempEndTime - tempEndTime > order.getTime()){
                int battery = openWindow.getBateryOnStart() + requiredBattery;
                int endBattery = battery - pathCalc;
                int batteryForAnotherOrder = getNewBattery(endBattery,tempEndTime - tempEndTime);
                if(batteryForAnotherOrder  <= openWindow.getExspected()){
                    return true;
                }
            }
        }else if(openWindow.getExspected() >= openWindow.getBateryOnStart() - pathCalc){
            return true;
        }
        return false;
    }

    public double getNewStartTime(int battery){
        return (battery / 5)*60_000;
    }

    public int getNewBattery(int battery,double time){
        double percentForMillis = 5/60/1000;
        double addBattery = percentForMillis * time;
        return  battery + (int)addBattery;

    }

    public void startProceduresForAdding(int index,double startTime,double endTime,int countDrones,OpenWindowsWithDroneId base){
        double tempStartTime = startTime;
        double tempEndTime = endTime;
        ArrayList<OpenWindowsWithDroneId> openWindows = new ArrayList<>();
        openWindows.add(base);
        z: for (int i = 0; i < drones.size() ; i++) {

            if(i != index){
               p:  for (int j = 0; j < drones.get(i).openWindows.size() ; j++) {
                   if(drones.get(i).openWindows.get(j).getStartTime() > tempEndTime){
                       tempStartTime = drones.get(i).openWindows.get(j).getStartTime();
                   }
                   if(drones.get(i).openWindows.get(j).getCurentTime() < tempEndTime){
                       tempEndTime = drones.get(i).openWindows.get(j).getCurentTime();
                   }
                   if(tempEndTime - tempStartTime < 0){
                       continue p;
                   }else {
                       OpenWindowsWithDroneId id = new OpenWindowsWithDroneId(drones.get(i).openWindows.get(j),i);
                       openWindows.add(id);
                       if(!multiAddOpenWindows(openWindows,tempStartTime,tempEndTime)){
                           openWindows.remove(id);
                       }else {
                           continue z;
                       }
                   }
                   if(countDrones  == openWindows.size()){
                       //startPut
                   }

                }
            }

        }
    }

    public boolean multiAddOpenWindows(ArrayList<OpenWindowsWithDroneId> openWindows,double startTime,double endTime){
        for (int i = 0; i < openWindows.size(); i++) {
            if(!isPotentialCandidate(startTime,endTime,openWindows.get(i).getOpenWindow())){
                return false;
            }
        }
        return true;
    }

    public void startPutingToOpenWindows(double startTime,double endTime){

    }


}
