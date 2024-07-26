import java.util.*;

public class sortStrings {

    public static void main(String[] args) {

        String names = "xqc,abugoku9999,adeptthebest,amar,annitheduck,arrowcs,bastighg,breitenberg," +
                "chefstrobel,chess,derfruchtzwergtv,di1araas,edizderbreite,eliasn97," +
                "elotrix,exsl95,filow,fritz_meinecke,gamerbrother,gebauermarc,gmhikaru,gothamchess," +
                "gronkh,honeypuu,inscope21tv,k4yfour,kaicenat,klexifuchs,kobrickrl," +
                "kuchentv,letshugotv,mckytv,mizkif,montanablack88,mshl_louis,nihachu," +
                "ninja,noahzett28,nooreax,ohnepixel,papaplatte,pietsmiet,pokelawls," +
                "pokimane,psp1g,quitelola,realmoji,reeze,revedtv,rewinside,rezo," +
                "ronnyberger,rumathra,scor_china,shurjoka,sidneyeweka,skylinetvlive,sodapoppin," +
                "sparkofphoenixtv,stegi,summit1g,tanzverbot,therealknossi,tobifas_," +
                "trainwreckstv,trymacs,unsympathisch_tv,fibii,xrohat," +
                "xthesolutiontv,zackrawrr,zarbex,zastela,handofblood,shlorox,missmikkaa," +
                "jayzumjiggy,loserfruit,elspreen,bratishkinoff,illojuan,mertabimula,"
                +"niklaswilson,wichtiger";
        String [] split = names.split(",");
        ArrayList<String> list = new ArrayList<>(Arrays.asList(split));

        Collections.sort(list);

        for (String s : list) {
            System.out.print(s +",");
        }
    }
}