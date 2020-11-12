package collectionClasses;


import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;

import collectionClasses.*;

public class LabWork implements Serializable {
    private static final long serialVersionUID = 6529685098267757690L;
    public int id; //Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    private String name; //Поле не может быть null, Строка не может быть пустой
    private Coordinates coordinates; //Поле не может быть null
    private ZonedDateTime creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    private float minimalPoint; //Значение поля должно быть больше 0
    private Double personalQualitiesMinimum; //Поле не может быть null, Значение поля должно быть больше 0
    private Difficulty difficulty; //Поле может быть null
    private Person author; //Поле не может быть null

    /**
     * @param idV add id of element into LinkedList
     * @param nameV add name of element into LinkedList
     * @param coordinatesV add coordinates of element into LinkedList
     * @param creationDateV add creation date of element into LinkedList
     * @param minimalPointV add value of minimal point of element into LinkedList
     * @param personalQualitiesMinimumV add value of personal qualities minimum of element into LinkedList
     * @param difficultyV add value difficulty of element into LinkedList
     * @param authorV add Person of element into LinkedList
     */
    public LabWork(int idV, String nameV, Coordinates coordinatesV, ZonedDateTime creationDateV, float minimalPointV, Double personalQualitiesMinimumV, Difficulty difficultyV, Person authorV) {
        id=idV;
        name=nameV;
        coordinates=coordinatesV;
        creationDate=creationDateV;
        minimalPoint=minimalPointV;
        personalQualitiesMinimum=personalQualitiesMinimumV;
        difficulty=difficultyV;
        author=authorV;
    }

    /**
     *
     * @return a string with all values of element of linkedlist. Regex ";"
     */
    public String getInfo(){
        return id+";"+name+";"+coordinates.getCoordinates()+";"+creationDate+";"+minimalPoint+";"+personalQualitiesMinimum+";"+difficulty+";"+author.getInfoPerson();
    }
    /**
     *
     * @return a string with id of element of linkedlist. Regex ";"
     */
    public int getId(){
        return id;
    }

    public void addNewId(int newId){
        id=newId;
    }

    /**
     *
     * @param nameGet used to set name in updated element
     */
    public void setName(String nameGet) {
        name = nameGet;
    }

    /**
     *
     * @param coordinatesGet used to set value of coordinate
     * @param type can be X or Y in lower case - mean type of coordinate
     */
    public void setCoordinates(Long coordinatesGet, String type) {
        if (type.equals("x")){
            coordinates.setX(coordinatesGet);
        } else {
            coordinates.setY(coordinatesGet);
        }
    }
    /**
     *
     * @param mpGet used to set minimal point in updated element
     */
    public void setMinimalPoint(float mpGet) {
        minimalPoint = mpGet;
    }
    /**
     *
     * @param pqmGet used to set personal quality in updated element
     */
    public void setPQM(Double pqmGet) {
        personalQualitiesMinimum = pqmGet;
    }
    /**
     *
     * @param difGet used to set Difficulty in updated element
     */
    public void setDif(Difficulty difGet) {
        difficulty = difGet;
    }
    /**
     *
     * @param aname used to set author's name in updated element
     */
    public void setAuthorName(String aname) {
        author.setAName(aname);
    }
    /**
     *
     * @param abday used to set author's birthday in updated element
     */
    public void setAuthorBday(LocalDate abday) {
        author.setABirth(abday);
    }
    /**
     *
     * @param aheight used to set author's height in updated element
     */
    public void setAuthorHeight(float aheight) {
        author.setAHeight(aheight);
    }
    /**
     *
     * @param cntr used to set author's nationality in updated element
     */
    public void setAuthorCountry(Country cntr) {
        author.setACountry(cntr);
    }

    /**
     *
     * @return value minimal point of element
     */
    public float getMinimalPoint(){
            return minimalPoint;
    }
    /**
     *
     * @return value personal quality of element
     */
    Double getPQM(){
        return personalQualitiesMinimum;
    }


}

