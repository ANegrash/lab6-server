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
    private static final int BUFFER_SIZE = 10000;
    int port = 13, port2=12;
    private DatagramSocket socket;
    boolean isComand=true, isSended=true;
    String comand, dataName;
    String[] data, historyData;
    LinkedList<LabWork> labwork;
    LinkedList<History> history;
    File base;
    LabWork lw;
    LabWork[] lw2= new LabWork[100];
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    public void createServer() throws IOException, ClassNotFoundException {

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
            InetAddress hostIP = InetAddress.getLocalHost();
            InetSocketAddress address = new InetSocketAddress(hostIP, port);
            DatagramChannel datagramChannel = DatagramChannel.open();
            DatagramSocket datagramSocket = datagramChannel.socket();
            datagramSocket.bind(address);
            datagramChannel.receive(buffer);
            datagramSocket.close();
            buffer.flip();
            if(isComand){
                String data = new String(buffer.array(), "UTF-8");
                isComand=false;
                buffer.clear();
                buffer.put(new byte[1024]);
                buffer.clear();
                comand = data;
                if (comand.contains("help")) {
                    System.out.println("\nПришла команда: help");
                    sendAnswer("help : вывести справку по доступным командам\n" +
                            "info : вывести в стандартный поток вывода информацию о коллекции (тип, количество элементов, вес файла и т.д.)\n" +
                            "show : вывести в стандартный поток вывода все элементы коллекции в строковом представлении\n" +
                            "add {element} : добавить новый элемент в коллекцию\n" +
                            "update_id {element} : обновить значение элемента коллекции, id которого равен заданному\n" +
                            "remove_by_id {id} : удалить элемент из коллекции по его id\n" +
                            "clear : очистить коллекцию\n" +
                            "exit : завершить программу (без сохранения в файл)\n" +
                            "add_if_max {element} : добавить новый элемент в коллекцию, если его значение превышает значение наибольшего элемента этой коллекции\n" +
                            "remove_lower {element} : удалить из коллекции все элементы, меньшие, чем заданный\n" +
                            "history : вывести последние 12 команд (без их аргументов)\n" +
                            "count_less_than_minimal_point {minimalPoint} : вывести количество элементов, значение поля minimalPoint которых меньше заданного\n" +
                            "filter_by_minimal_point {minimalPoint} : вывести элементы, значение поля minimalPoint которых равно заданному\n" +
                            "filter_less_than_personal_qualities_minimum {personalQualitiesMinimum} : вывести элементы, значение поля personalQualitiesMinimum которых меньше заданного");
                    isComand=true;
                    addToHistory("help");
                }else if(comand.contains("add_if_max")) {
                    addToHistory("add_if_max");
                    System.out.println("\nПришла команда: add_if_max");
                    sendAnswer("Добавление нового элемента в коллекцию:");

                }else if(comand.contains("add")) {
                    addToHistory("add");
                    System.out.println("\nПришла команда: add");
                    sendAnswer("Добавление нового элемента в коллекцию:");
                }else if(comand.contains("clear")) {
                    System.out.println("\nПришла команда: clear");
                    isComand=true;
                    addToHistory("clear");
                    labwork.clear();
                    saveData(labwork);
                    sendAnswer("Коллекция успешно очищена");
                }else if(comand.contains("count_less_than_minimal_point")) {
                    System.out.println("\nПришла команда: count_less_than_minimal_point");
                    addToHistory("count_less_than_minimal_point");
                    sendAnswer("Введите значение minimal point для сравнения (тип Float, больше или равно 0):");
                }else if(comand.contains("filter_by_minimal_point")) {
                    System.out.println("\nПришла команда: filter_by_minimal_point");
                    sendAnswer("Введите значение minimal point для фильтрации (тип Float, больше или равно 0):");
                }else if(comand.contains("filter_less_than_personal_qualities_minimum")) {
                    System.out.println("\nПришла команда: filter_less_than_personal_qualities_minimum");
                    sendAnswer("Введите значение personal quality minimum");
                }else if(comand.contains("history")) {
                    System.out.println("\nПришла команда: history");
                    String toSend="";
                    for(History h : history){
                        toSend=toSend+h.getToWrite()+"\n";
                    }
                    sendAnswer(toSend);
                    isComand=true;
                }else if(comand.contains("info")) {
                    System.out.println("\nПришла команда: info");
                    String myAnswer= "Тип коллекции: LinkedList\n"+
                    "Количество элементов коллекции: " + labwork.size()+"\n" +
                    "Абсолютный путь файла хранения коллекции: " + base.getAbsolutePath()+"\n" +
                    "Вес файла хранения коллекции: " + base.length() + " байт";
                    sendAnswer(myAnswer);
                    isComand=true;
                    addToHistory("info");
                }else if(comand.contains("remove_by_id")) {
                    System.out.println("\nПришла команда: remove_by_id");
                    sendAnswer("Введите id элемента, который нужно удалить (целое число, больше 0):");
                }else if(comand.contains("remove_lower")) {
                    System.out.println("\nПришла команда: remove_lower");
                    sendAnswer("Добавление нового элемента в коллекцию:");
                }else if(comand.contains("show")) {
                    System.out.println("\nПришла команда: show");
                    String myAnswer ="";
                    for (LabWork p : labwork) {
                        myAnswer=myAnswer+p.getInfo()+"\n";
                    }
                    if (myAnswer.length()==0) myAnswer="Казна пуста, милорд";
                    sendAnswer(myAnswer);
                    isComand=true;
                    addToHistory("show");

                }else if(comand.contains("update_id")) {
                    System.out.println("\nПришла команда: update_id");
                    sendAnswer("Введите id элемента, который хотите изменить");
                    addToHistory("update_id");
                }else {
                    isComand=true;
                    System.err.println("\nПришла неизвестная команда");
                    sendAnswer("Мы не нашли такую команду. Введите help для вызова списка команд");
                }
                buffer.clear();

            }else{
                if(comand.contains("add_if_max")) {
                    isComand=true;
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
                        sendAnswer("Введённое значение не было максимальным...");
                    }else {
                        sendAnswer("Введённое значение добавлено, поскольку максимально!");
                    }
                    saveData(labwork);
                }
                else if(comand.contains("add")) {
                    isComand=true;
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

                }
                else if(comand.contains("count_less_than_minimal_point")) {
                    isComand=true;
                    String datas = new String(buffer.array(), "UTF-8");
                    float getMinimalPoint = Float.parseFloat(datas);
                    int counter = 0;
                    for (LabWork p : labwork) {
                        if (getMinimalPoint > p.getMinimalPoint())
                            counter++;
                    }
                    sendAnswer("Количество элементов, значение поля minimalPoint которых меньше заданного: " + counter);
                }
                else if(comand.contains("filter_by_minimal_point")) {
                    isComand=true;
                    String datas = new String(buffer.array(), "UTF-8");
                    float getMinimalPoint = Float.parseFloat(datas);
                    String toSend="";
                    for (LabWork p : labwork) {
                        if (getMinimalPoint == p.getMinimalPoint()) toSend=toSend+p.getInfo()+"\n";
                    }
                    if(toSend.isEmpty()) toSend="Элементы не найдены";
                    sendAnswer(toSend);
                }
                else if(comand.contains("filter_less_than_personal_qualities_minimum")) {
                    isComand=true;
                    String datas69 = "";
                    double getPQM = buffer.getDouble();
                    for (LabWork p : labwork) {
                        if (getPQM > p.getPQM()) datas69=datas69+p.getInfo()+"\n";
                    }
                    if(datas69.isEmpty()) datas69="Элементы не найдены";
                    sendAnswer(datas69);
                }
                else if(comand.contains("remove_by_id")) {
                    isComand=true;
                    int getIdToRemove =buffer.getInt();
                    boolean completed = false;
                    for (LabWork p : labwork) {
                        if (getIdToRemove == p.getId()){
                            labwork.remove(p);
                            completed=true;
                        }
                    }
                    if(completed){
                        sendAnswer("Удаление прошло успешно");
                    }else {
                        sendAnswer("Удаление не произошло. Возможно Вы указали несуществующий id");
                    }
                    saveData(labwork);
                }
                else if(comand.contains("remove_lower")) {
                    isComand=true;
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
                    sendAnswer("Всего найдено и удалено "+counter+ " элементов");
                    saveData(labwork);

                }
                else if(comand.contains("update_id")) {
                    if(isSended){
                        int getIdToUpd =buffer.getInt();
                        boolean completed = false;
                        String myCol="";
                        for (LabWork p : labwork) {
                            if (getIdToUpd == p.getId()){
                                myCol=p.getInfo();
                                completed=true;
                            }
                        }
                        if(completed){
                            sendAnswer("Какое поле вы бы хотели изменить? \n(id, name, coordinates.x, coordinates.y, creationDate, minimalPoint, personalQualitiesMinimum, difficulty, author.name, author.birthday, author.height, author.nationality)\n"+myCol);
                            isComand=false;
                            isSended=false;
                        }else {
                            sendAnswer("Ошибка. Вы указали несуществующий id");
                            isComand=true;
                        }
                    }else{


                    }
                }

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
    public void sendAnswer(String answer) throws IOException{
        InetAddress hostIP = InetAddress.getLocalHost();
        InetSocketAddress myAddress2 = new InetSocketAddress(hostIP, port2);
        DatagramChannel datagramChannel2 = DatagramChannel.open();
        //datagramChannel.bind(myAddress);

        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        buffer.put(answer.getBytes());
        buffer.flip();
        System.out.println("Отправка данных...");
        datagramChannel2.send(buffer, myAddress2);
        System.out.println("Отправка завершена.");
        buffer.clear();

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