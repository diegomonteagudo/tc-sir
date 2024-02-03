package com.insatc.sir_scanner.datatools;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Tout ça sert juste à créer le .json avec GSON... Je n'ai pas trouvé plus simple
 * GSON ne marche pas autrement donc obligé de faire ces classes. On en profite pour
 * l'affichage des données dans l'application.
 */
public class RecordingData {
    public String nomEnregistrement;
    public String technologieVisee;
    public String operateur;
    public String dureePrevue;
    public String debut;
    public String fin;
    public String intervalleMesure;
    public int nombreMesures;
    public int nombreCellulesServing;
    public int nombreCellulesTotal;
    public List<Integer> listeZones;
    public List<Cellule> listeCellulesServing; //cellules qui ont servi de Serving Cell au moins 1 fois
    public List<Cellule> listeCellulesVoisinnes; //cellules qui ne sont apparues qu'en tant que voisinnes

    public RecordingData(){
        nomEnregistrement = null;
        technologieVisee = null;
        operateur = null;
        dureePrevue = null;
        debut = null;
        fin = null;
        intervalleMesure = null;
        listeZones = new LinkedList<>();
        listeCellulesServing = new LinkedList<>();
        listeCellulesVoisinnes = new LinkedList<>();
    }

    /**
     * Ajouter ou mettre à jour une serving cell 4G
     * @param ci ci
     * @param pci physical cell id (utilisé ici comme un id secondaire)
     * @param earfcn earfcn
     * @param mcc mcc
     * @param mnc mnc
     * @param tac tac
     * @return
     */
    public Cellule register4GServingCell(int ci, int pci, int earfcn, int mcc, int mnc, int tac){
        Cellule cell = null;

        //ajouter la zone de localisation si elle n'y est pas
        if(!listeZones.contains(tac)){
            listeZones.add(tac);
        }

        //si la cellule a déjà été serving on incrémente son compteur
        for(Cellule cellElement : listeCellulesServing){
            if(cellElement.idPrimaire == ci && cellElement.technologie.equals("4G")){
                cellElement.nbPointsServing+=1;
                cellElement.nbPointsApparition+=1;
                cell = cellElement;
                return cell;
            }
        }

        /*ATTENTION : pour les cellules voisinnes on a que le PCI. Possibilité rare de 2 cellules
        différentes avec le même PCI. En plus ça dépend peut-être des téléphones le fait d'avoir que le PCI.
        Réfléchir à l'enlever ou pas*/
        for(Cellule cellElement : listeCellulesVoisinnes){
            if(cellElement.idSecondaire == pci && cellElement.technologie.equals("4G")){
                listeCellulesVoisinnes.remove(cellElement);
                listeCellulesServing.add(cellElement);
                cellElement.nbPointsServing=+1;
                cellElement.nbPointsApparition+=1;
                cellElement.idPrimaire = ci;
                cellElement.MCC = mcc;
                cellElement.MNC = mnc;
                cellElement.zone = tac;
                cellElement.xRFCN = earfcn;
                nombreCellulesServing += 1;
                cell = cellElement;
                return cell;
            }
        }


        //la cellule n'a jamais été vue auparavant
        cell = new Cellule("4G",ci, pci, mcc, mnc, tac);
        cell.nbPointsServing+=1;
        cell.nbPointsApparition+=1;
        cell.xRFCN = earfcn;
        listeCellulesServing.add(cell);
        nombreCellulesServing += 1;
        nombreCellulesTotal += 1;
        return cell;

    }


    /**
     * Ajouter ou mettre à jour une serving cell 3G
     * @param cid cid
     * @param psc primary scrambling code (utilisé ici comme un id secondaire)
     * @param uarfcn uarfcn
     * @param mcc mcc
     * @param mnc mnc
     * @param lac lac
     * @return
     */
    public Cellule register3GServingCell(int cid, int psc, int uarfcn, int mcc, int mnc, int lac){
        Cellule cell = null;

        //ajouter la zone de localisation si elle n'y est pas
        if(!listeZones.contains(lac)){
            listeZones.add(lac);
        }

        //si la cellule a déjà été serving on incrémente son compteur
        for(Cellule cellElement : listeCellulesServing){
            if(cellElement.idPrimaire == cid && cellElement.technologie.equals("3G")){
                cellElement.nbPointsServing+=1;
                cellElement.nbPointsApparition+=1;
                cell = cellElement;
                return cell;
            }
        }

        /*ATTENTION : même problème que pour 4G*/
        //en 3G j'ai l'impression que la seule manière de caractériser une cellule
        //avec le peu d'infos que donne TelephonyManager c'est le couple (cid,psc)...
        for(Cellule cellElement : listeCellulesVoisinnes){
            if(cellElement.idSecondaire == psc && cellElement.idPrimaire == cid
                    && cellElement.technologie.equals("3G")){
                listeCellulesVoisinnes.remove(cellElement);
                listeCellulesServing.add(cellElement);
                cellElement.nbPointsServing=+1;
                cellElement.nbPointsApparition+=1;
                cellElement.idPrimaire = cid;
                cellElement.MCC = mcc;
                cellElement.MNC = mnc;
                cellElement.zone = lac;
                cellElement.xRFCN = uarfcn;
                nombreCellulesServing += 1;
                cell = cellElement;
                return cell;
            }
        }

        //la cellule n'a jamais été vue auparavant
        cell = new Cellule("3G",cid, psc, mcc, mnc, lac);
        cell.xRFCN = uarfcn;
        cell.nbPointsServing+=1;
        cell.nbPointsApparition+=1;
        cell.xRFCN = uarfcn;
        listeCellulesServing.add(cell);
        nombreCellulesServing += 1;
        nombreCellulesTotal += 1;
        return cell;

    }


    /**
     * Ajouter ou mettre à jour une serving cell 2G
     * @param cid cid
     * @param bsic base station identity code (utilisé ici comme un id secondaire)
     * @param arfcn arfcn
     * @param mcc mcc
     * @param mnc mnc
     * @param lac lac
     * @return
     */
    public Cellule register2GServingCell(int cid, int bsic, int arfcn, int mcc, int mnc, int lac){
        Cellule cell = null;

        //ajouter la zone de localisation si elle n'y est pas
        if(!listeZones.contains(lac)){
            listeZones.add(lac);
        }

        //si la cellule a déjà été serving on incrémente son compteur
        for(Cellule cellElement : listeCellulesServing){
            if(cellElement.idPrimaire == cid && cellElement.idSecondaire == bsic
                    && cellElement.technologie.equals("2G")){
                cellElement.nbPointsServing+=1;
                cellElement.nbPointsApparition+=1;
                cell = cellElement;
                return cell;
            }
        }

        /*ATTENTION : En 2G on a souvent le CID et le BSIC pour
        * les cellules voisinnes donc j'utilise les deux pour identifier les cellules voisinnes.
        * Par contre pour certaines on a ni l'un ni l'autre (le CID est à 65535 et le BSIC à rien))
        * donc pour nous c'est comme si elles n'existaient pas.
         */
        for(Cellule cellElement : listeCellulesVoisinnes){
            if(cellElement.idSecondaire == bsic
                    && cellElement.technologie.equals("2G")){
                listeCellulesVoisinnes.remove(cellElement);
                listeCellulesServing.add(cellElement);
                cellElement.nbPointsServing=+1;
                cellElement.nbPointsApparition+=1;
                cellElement.idPrimaire = cid;
                cellElement.MCC = mcc;
                cellElement.MNC = mnc;
                cellElement.zone = lac;
                cellElement.xRFCN = arfcn;
                nombreCellulesServing += 1;
                cell = cellElement;
                return cell;
            }
        }

        //la cellule n'a jamais été vue auparavant
        cell = new Cellule("2G",cid, bsic, mcc, mnc, lac);
        cell.nbPointsServing+=1;
        cell.nbPointsApparition+=1;
        cell.xRFCN = arfcn;
        listeCellulesServing.add(cell);
        nombreCellulesServing += 1;
        nombreCellulesTotal += 1;
        return cell;

    }


    /**
     * Ajoute un point GPS à la cellule serving correspondante
     * @param id1 id1 selon la technologie (voir Cellule) pour identifier la cellule
     * @param id2 id2 selon la technologie (voir Cellule) pour identifier la cellule
     * @param latitude latitude
     * @param longitude longitude
     * @param altitude altitude
     * @param precision précision
     */
    public void addGPSPointToServingCell(int id1, int id2, double latitude, double longitude, double altitude, double precision){
        for(Cellule cellElement : listeCellulesServing){
            if(cellElement.idPrimaire == id1 && cellElement.idSecondaire == id2){
                cellElement.addGPSPoint(latitude, longitude, altitude, precision);
            }
        }
    }

    /**
     * Ajoute les infos de signal 4G LTE moyennes à la cellule serving correspondante
     * ATTENTION : appeler uniquement après avoir registered toutes les apparitions de la cellule
     * @param id1 id1 selon la technologie (voir Cellule) pour identifier la cellule
     * @param id2 id2 selon la technologie (voir Cellule) pour identifier la cellule
     * @param puissance puissance en dBm
     * @param rsrp rsrp en dBm
     * @param rsrq rsrq en dBm
     */
    public void addSignalInfoTo4GServingCell(int id1, int id2, double puissance, double rsrp, double rsrq){
        for(Cellule cellElement : listeCellulesServing){
            if(cellElement.idPrimaire == id1 && cellElement.idSecondaire == id2){
                cellElement.addSignalInfoServing(puissance, rsrp, rsrq);
            }
        }
    }

    public void addSignalInfoToServingCell(int id1, int id2, double puissance){
        for(Cellule cellElement : listeCellulesServing){
            if(cellElement.idPrimaire == id1 && cellElement.idSecondaire == id2){
                cellElement.addSignalInfoServing(puissance);
            }
        }
    }


    /**
     * Trie la liste des cellules serving par ordre décroissant de nbPointsServing
     */
    public void sortServingCells(){
        List<Cellule> sortedList = new LinkedList<>();
        int max = 0;
        Cellule maxCell = null;
        while(listeCellulesServing.size() > 0){
            for(Cellule cell : listeCellulesServing){
                if(cell.nbPointsServing > max){
                    max = cell.nbPointsServing;
                    maxCell = cell;
                }
            }
            sortedList.add(maxCell);
            listeCellulesServing.remove(maxCell);
            max = 0;
        }
        listeCellulesServing = sortedList;
        for(Cellule cell : listeCellulesServing){
            System.out.println(cell.nbPointsServing);
        }
    }


    /**
     * Retourne l'id1 des cellules serving avec le pourcentage de fois qu'elles ont served
     */
    public LinkedHashMap<Integer, Double> getServingCellsPercentage(){
        LinkedHashMap<Integer, Double> map = new LinkedHashMap<>();
        for(Cellule cell : listeCellulesServing){
            map.put(cell.idPrimaire, ((double)cell.nbPointsServing / (double)nombreMesures)*100);
            System.out.println(((double)cell.nbPointsServing / (double)nombreMesures)*100);
        }
        return map;
    }


    /**
     * Met à jour le nombre de cellules serving et total
     */
    public void updateNbCellules(){
        nombreCellulesTotal = listeCellulesServing.size() + listeCellulesVoisinnes.size();
        nombreCellulesServing = listeCellulesServing.size();
    }

    public class Cellule {
        String technologie;
        int idPrimaire; //CI pour 4G, CID pour 2G et 3G
        int idSecondaire; //PCI (4G), PSC (3G même si pas un ID), BSIC (2G)
        int MCC;
        int MNC;
        int zone; //TAC (4G), LAC (2G,3G)
        int xRFCN; //EARFCN (4G), UARFCN (3G), ARFCN (2G)
        int nbPointsServing = 0;
        int nbPointsApparition = 0;
        double moyennePuissance = 0;
        double moyenneRSRP = 0;
        double moyenneRSRQ = 0;
        List<PointGPS> pointsGPSServing;

        public Cellule() {
            this.technologie = "";
            this.idPrimaire = -1;
            this.idSecondaire = -1;
            this.MCC = -1;
            this.MNC = -1;
            this.zone = -1;
            this.xRFCN = -1;
            this.pointsGPSServing = new LinkedList<>();
        }

        public Cellule(String tech, int id1, int id2, int mcc, int mnc, int zone){
            this.technologie = tech;
            this.idPrimaire = id1;
            this.idSecondaire = id2;
            this.MCC = mcc;
            this.MNC = mnc;
            this.zone = zone;
            this.pointsGPSServing = new LinkedList<>();
        }

        public void addGPSPoint(double latitude, double longitude, double altitude, double precision){
            pointsGPSServing.add(new PointGPS(latitude, longitude, altitude, precision));
        }

        //uniquement une fois que nbPointsServing est final
        public void addSignalInfoServing(double puissance, double rsrp, double rsrq){
            moyennePuissance += puissance / nbPointsServing;
            moyenneRSRP += rsrp / nbPointsServing;
            moyenneRSRQ += rsrq / nbPointsServing;
        }

        //uniquement une fois que nbPointsServing est final. Version sans RSRP et RSRQ (2G,3G)
        public void addSignalInfoServing(double puissance){
            moyennePuissance += puissance / nbPointsServing;
        }

        public class PointGPS {
            double latitude;
            double longitude;
            double altitude;
            double precision;

            PointGPS(double latitude, double longitude, double altitude, double precision) {
                this.latitude = latitude;
                this.longitude = longitude;
                this.altitude = altitude;
                this.precision = precision;
            }
        }
    }

}
