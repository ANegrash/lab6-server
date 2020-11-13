import collectionClasses.*;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Locale;

public class Connect {
    private static final int BUFFER_SIZE = 1024;
    int port;
    boolean isComand=true;
    String comand, dataName;
    String[] data, historyData;
    LinkedList<LabWork> labwork;
    LinkedList<History> history;
    File base;
    LabWork[] lw2= new LabWork[100];
    LabWork lw;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    InetSocketAddress address, myAddress;
    DatagramChannel datagramChannel;
    DatagramSocket s;
    InetAddress inetAddress;


    public void createServer() throws IOException, ClassNotFoundException {

        SocketAddress a = new InetSocketAddress(InetAddress.getLocalHost(),8888);
        s = new DatagramSocket();

        System.out.println("Сервер запущен");

            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        labwork = new LinkedList<>();
        history = new LinkedList<>();
        dataName = "labdata.csv";//System.getenv("LAB_NEGRASH");
        base = new File(dataName);
        formatter = formatter.withLocale(Locale.US);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(dataName));
            String line;
            while ((line = reader.readLine()) != null) {
                int index = line.lastIndexOf(';');
                if (index == -1) {
                    System.err.println("Файл с данными пустой");
                } else {
                    data = line.split(";");
                    int id = Integer.parseInt(data[0]);
                    String name = data[1];
                    Long x = Long.parseLong(data[2]);
                    Long y = Long.parseLong(data[3]);
                    ZonedDateTime creationDate = ZonedDateTime.parse(data[4]);
                    float minimalPoint = Float.parseFloat(data[5]);
                    Double personalQualitiesMinimum = Double.parseDouble(data[6]);
                    Difficulty difficulty = Difficulty.valueOf(data[7]);
                    String nameAuthor = data[8];
                    LocalDate birthday = LocalDate.parse(data[9], formatter);
                    float height = Float.parseFloat(data[10]);
                    Country nationality = Country.valueOf(data[11]);

                    labwork.add(new LabWork(id, name, new Coordinates(x,y), creationDate, minimalPoint, personalQualitiesMinimum, difficulty, new Person(nameAuthor, birthday, height, nationality)));
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader("history.csv"));
            String line;
            while ((line = reader.readLine()) != null) {
                int index = line.lastIndexOf(';');
                if (index == -1) {
                    System.err.println("Файл с историей пустой");
                } else {
                    historyData = line.split(";");
                    ZonedDateTime creationDate = ZonedDateTime.parse(historyData[0]);
                    String whatAct = historyData[1];

                    history.add(new History(creationDate, whatAct));
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


            while (true) {
                //Receive a packet
                byte[] recvBuf = new byte[15000];
                DatagramPacket i = new DatagramPacket(recvBuf, recvBuf.length);
                System.out.println("Сервер запущен"+recvBuf.length);
                s.receive(i);
                inetAddress=i.getAddress();
                port =i.getPort();
                System.out.println("Хост: "+inetAddress+"\nПорт: "+ port);

                if(isComand){
                    String message = new String(i.getData()).trim();
                    isComand=false;
                    comand = message;
                    System.out.println("\nПришла команда: " + comand);
                    if (comand.contains("help")) {
                        isComand=true;
                        addToHistory("help");
                    }else if(comand.contains("add_if_max")) {
                        addToHistory("add_if_max");
                    }else if(comand.contains("add")) {
                        addToHistory("add");
                    }else if(comand.contains("clear")) {
                        isComand=true;
                        addToHistory("clear");
                        labwork.clear();
                        saveData(labwork);
                    }else if(comand.contains("count_less_than_minimal_point")) {
                        addToHistory("count_less_than_minimal_point");
                    }else if(comand.contains("filter_by_minimal_point")) {

                    }else if(comand.contains("filter_less_than_personal_qualities_minimum")) {

                    }else if(comand.contains("history")) {
                        addToHistory("history");
                        String getHi = "История:\n";
                        for(History h : history){
                            getHi=getHi+h.getToWrite()+"\n";
                        }
                        sendInfo(getHi);
                    }else if(comand.contains("info")) {
                        addToHistory("info");
                        String dataI;
                        dataI = "Тип коллекции: LinkedList\n"+
                                "Количество элементов коллекции: " + labwork.size()+
                                "\nАбсолютный путь файла хранения коллекции: " + base.getAbsolutePath()+
                                "\nВес файла хранения коллекции: " + base.length() + " байт";
                        sendInfo(dataI);
                    }else if(comand.contains("remove_by_id")) {
                        addToHistory("remove_by_id");

                    }else if(comand.contains("remove_lower")) {
                        addToHistory("remove_lower");

                    }else if(comand.contains("show")) {

                    }else if(comand.contains("update_id")) {

                    }
                    buffer.clear();

                }else{
                    if(comand.contains("add_if_max")) {
                        int maxVal = 0;
                        for (LabWork p : labwork) {
                            if (p.getInfo().length() > maxVal) maxVal = p.getInfo().length();
                        }
                        final byte[] bytes = new byte[buffer.remaining()];
                        buffer.duplicate().get(bytes);
                        LabWork getLW = (LabWork) getObject(bytes);
                        isComand=true;
                        if (labwork.isEmpty()){
                            getLW.addNewId(1);
                        }else {
                            getLW.addNewId(labwork.getLast().getId() + 1);
                        }
                        System.out.println("\nПолученные данные: "+getLW.getInfo());
                        labwork.add(getLW);
                        if (labwork.getLast().getInfo().length() <= maxVal) {
                            labwork.remove(labwork.getLast());
                            sendInfo("Введённое значение не было максимальным...");
                        }else {
                            sendInfo("Введённое значение добавлено, поскольку максимально!");
                        }
                        saveData(labwork);
                    }else if(comand.contains("add")) {
                        addToHistory("add");
                        final byte[] bytes = new byte[buffer.remaining()];
                        buffer.duplicate().get(bytes);
                        LabWork getLW = (LabWork) getObject(bytes);
                        isComand=true;
                        if (labwork.isEmpty()){
                            getLW.addNewId(1);
                        }else {
                            getLW.addNewId(labwork.getLast().getId() + 1);
                        }
                        System.out.println("\nПолученные данные: "+getLW.getInfo());
                        labwork.add(getLW);
                        saveData(labwork);

                    }else if(comand.contains("count_less_than_minimal_point")) {
                        float getMinimalPoint = buffer.duplicate().getFloat();
                        int counter = 0;
                        for (LabWork p : labwork) {
                            if (getMinimalPoint > p.getMinimalPoint())
                                counter++;
                        }
                        sendInfo("Количество элементов, значение поля minimalPoint которых меньше заданного: " + counter);
                    }else if(comand.contains("filter_by_minimal_point")) {

                    }else if(comand.contains("filter_less_than_personal_qualities_minimum")) {

                    }else if(comand.contains("remove_by_id")) {
                        int getIdToRemove = buffer.getInt();
                        boolean completed = false;
                        for (LabWork p : labwork) {
                            if (getIdToRemove == p.getId()){
                                labwork.remove(p);
                                completed=true;
                            }
                        }
                        saveData(labwork);

                    }else if(comand.contains("remove_lower")) {
                        int myVal, counter=0;

                        final byte[] bytes = new byte[buffer.remaining()];
                        buffer.duplicate().get(bytes);
                        LabWork getLW = (LabWork) getObject(bytes);
                        isComand=true;
                        if (labwork.isEmpty()){
                            getLW.addNewId(1);
                        }else {
                            getLW.addNewId(labwork.getLast().getId() + 1);
                        }
                        System.out.println("\nПолученные данные: "+getLW.getInfo());
                        labwork.add(getLW);
                        myVal=labwork.getLast().getInfo().length();

                        for (LabWork p : labwork) {
                            if (p.getInfo().length() < myVal) {
                                lw2[counter]=p;
                                counter++;
                            }
                        }

                        for (int j = 0; j<lw2.length; j++){
                            labwork.remove(lw2[j]);
                        }
                        labwork.remove(labwork.getLast());
                        sendInfo("Всего найдено и удалено "+counter+ " элементов");
                        saveData(labwork);
                    }else if(comand.contains("update_id")) {

                    }

                    isComand=true;
                    buffer.clear();
                    if(comand.contains("update_id")) {
                        isComand=false;
                    }

                }
                buffer.clear();
            }


    }

    private static Object getObject(byte[] byteArr) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(byteArr);
        ObjectInput in = new ObjectInputStream(bis);
        return in.readObject();
    }

    public void addToHistory(String myComand){
        ZonedDateTime now = ZonedDateTime.now();
        if (history.size() >= 12) {
            history.removeLast();
        }
        history.addFirst(new History(now, myComand));

        try(PrintWriter pw = new PrintWriter("history.csv"))
        {
            for(History h : history){

                pw.println(h.getToWrite());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void saveData(LinkedList lw){
        try(PrintWriter pw = new PrintWriter(dataName))
        {
            for(LabWork h : labwork){

                pw.println(h.getInfo());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void sendAnswer(){

    }

    public void sendInfo(String data) throws IOException {
        byte[] sendData = data.getBytes();

        DatagramPacket o = new DatagramPacket(sendData, sendData.length, inetAddress, port);
        s.send(o);

    }
}
