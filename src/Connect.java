import collectionClasses.*;

import java.io.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Locale;

public class Connect {
    private static final int BUFFER_SIZE = 1024;
    static int port = 11;
    private DatagramSocket socket;
    boolean isComand=true;
    String comand, dataName;
    String[] data, historyData;
    LinkedList<LabWork> labwork;
    LinkedList<History> history;
    File base;
    LabWork lw;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    public void createServer() throws IOException, ClassNotFoundException {

            InetAddress hostIP = InetAddress.getLocalHost();
            InetSocketAddress address = new InetSocketAddress(hostIP, port);
            DatagramChannel datagramChannel = DatagramChannel.open();
            DatagramSocket datagramSocket = datagramChannel.socket();
            datagramSocket.bind(address);

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
                datagramChannel.receive(buffer);
                buffer.flip();
                if(isComand){
                    String data = new String(buffer.array(), "UTF-8");
                    isComand=false;
                    comand = data;
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

                    }else if(comand.contains("info")) {

                    }else if(comand.contains("remove_by_id")) {

                    }else if(comand.contains("remove_lower")) {

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
                            System.out.println("Введённое значение не было максимальным...");
                        }else {
                            System.out.println("Введённое значение добавлено, поскольку максимально!");
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
                        System.out.println("Количество элементов, значение поля minimalPoint которых меньше заданного: " + counter);
                    }else if(comand.contains("filter_by_minimal_point")) {

                    }else if(comand.contains("filter_less_than_personal_qualities_minimum")) {

                    }else if(comand.contains("remove_by_id")) {

                    }else if(comand.contains("remove_lower")) {

                    }else if(comand.contains("update_id")) {

                    }

                    isComand=true;
                    buffer.clear();

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
}
