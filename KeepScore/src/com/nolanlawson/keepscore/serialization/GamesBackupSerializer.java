package com.nolanlawson.keepscore.serialization;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.content.ContentResolver;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Xml;

import com.nolanlawson.keepscore.db.Game;
import com.nolanlawson.keepscore.db.PlayerScore;
import com.nolanlawson.keepscore.helper.SdcardHelper.Format;
import com.nolanlawson.keepscore.helper.XmlHelper;
import com.nolanlawson.keepscore.util.CollectionUtil;
import com.nolanlawson.keepscore.util.StringUtil;
import com.nolanlawson.keepscore.util.UtilLogger;

/**
 * helper classes for serializing/deserializing Games and PlayerScores.
 * 
 * @author nolan
 * 
 */
public class GamesBackupSerializer {

    /** Version without "automatic" - i.e., everything was manual */
    public static final int VERSION_ONE = 1;
    
    /** Version where "automatic" was added, to distinguish automatic backups from manual backups */
    public static final int VERSION_TWO = 2;
    
    /** 
     * Version where "backupFilename" was added, for cases where the filename could not easily be determined
     * (e.g. from Gmail attachments)
     */
    public static final int VERSION_THREE = 3;
    
    /**
     * Version where the lastUpdate was added.
     */
    public static final int VERSION_FOUR = 4;
    
    public static final int CURRENT_VERSION = VERSION_FOUR;
    
    private static final String ATTRIBUTE_NULL = "isNull";
    private static final String ATTRIBUTE_EMPTY = "isEmpty";

    private static UtilLogger log = new UtilLogger(GamesBackupSerializer.class);
    
    private static enum Tag {
        PlayerScore, Game, GamesBackup, gameCount, version, automatic, backupFilename, dateGameSaved, 
        dateBackupSaved, dateGameStarted, gameName, playerName, score, playerNumber, history, lastUpdate, 
        Games, PlayerScores;
    }

    /**
     * Don't read the entire file; just read the game count and other basic, summarized information.
     * 
     * @param backupFilename
     * @return
     */
    public static GamesBackupSummary readGamesBackupSummary(Uri uri, Format format, ContentResolver contentResolver) {
        
        GamesBackupSummary result = new GamesBackupSummary();
        
        int infoReceived = 0;
        
        try {

            XmlPullParser parser = null;
            BufferedReader reader = null;
            int parserEvent = -1;
            try {
                XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
                parser = parserFactory.newPullParser();
                InputStream inputStream = contentResolver.openInputStream(uri);
                if (format == Format.GZIP) { // new, gzipped format
                    inputStream = new GZIPInputStream(inputStream);
                }
                reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 0x1000);
                parser.setInput(reader);
                parserEvent = parser.getEventType();
                Tag tag = null;
                String text = null;
                
                while (parserEvent != XmlPullParser.END_DOCUMENT) {
                    parserEvent = parser.next();
                    switch (parserEvent) {
                        case XmlPullParser.START_TAG:
                            tag = Tag.valueOf(parser.getName());
                            break;
                        case XmlPullParser.TEXT:
                            text = parser.getText();
                            break;
                        case XmlPullParser.END_TAG:
                            switch (tag) {
                                case gameCount:
                                    result.setGameCount(Integer.parseInt(text));
                                    infoReceived++;
                                    break;
                                case version:
                                    result.setVersion(Integer.parseInt(text));
                                    infoReceived++;
                                    if (result.getVersion() < VERSION_TWO) {
                                        // no automatic vs. manual distinction in version one
                                        result.setAutomatic(false);
                                        infoReceived++;
                                    }
                                    if (result.getVersion() < VERSION_THREE) {                         
                                        // filename not stored in XML file itself until version three
                                        result.setFilename(uri.getLastPathSegment());
                                        infoReceived++;
                                    }
                                    break;
                                case automatic:
                                    result.setAutomatic(Boolean.parseBoolean(text));
                                    infoReceived++;
                                    break;
                                case dateBackupSaved:
                                    result.setDateSaved(Long.parseLong(text));
                                    infoReceived++;
                                    break;
                                case backupFilename:
                                    result.setFilename(text);
                                    infoReceived++;
                                    break;
                            }
                        break;
                    }
                    
                    if (infoReceived == 5) {
                        // this is all the info required to create a summary
                        return result;
                    }
                }
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        } catch (NumberFormatException e) {
            log.e(e, "unexpected exception for " + uri);
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.e(e, "unexpected exception for " + uri);
            throw new RuntimeException(e);
        } catch (XmlPullParserException e) {
            log.e(e, "unexpected exception for " + uri);
            throw new RuntimeException(e);
        }
        throw new RuntimeException("failed to find summary for " + uri);
    }

    public static GamesBackup deserialize(String xmlData) {
        int parserEvent = -1;
        XmlPullParser parser = null;

        Tag tag = null;

        GamesBackup gamesBackup = new GamesBackup();
        gamesBackup.setGames(new ArrayList<Game>());
        Game game = null;
        PlayerScore playerScore = null;
        Map<String, String> attributes = null;

        try {
            // calls service (referenced in url) to request XML serialized data
            parser = XmlHelper.loadData(xmlData);
            parserEvent = parser.getEventType();

            while (parserEvent != XmlPullParser.END_DOCUMENT) {
                switch (parserEvent) {
                case XmlPullParser.START_TAG:
                    tag = Tag.valueOf(parser.getName());
                    switch (tag) {
                    case Game:
                        game = new Game();
                        game.setPlayerScores(new ArrayList<PlayerScore>());
                        break;
                    case PlayerScore:
                        playerScore = new PlayerScore();
                        break;
                    }
                    // null or empty marker
                    if (parser.getAttributeCount() != -1) {
                        attributes = getAttributes(parser);
                    }
                    break;
                case XmlPullParser.END_TAG:
                    tag = Tag.valueOf(parser.getName());
                    switch (tag) {
                    case Game:
                        gamesBackup.getGames().add(game);
                        break;
                    case PlayerScore:
                        game.getPlayerScores().add(playerScore);
                        break;
                    }
                    break;
                case XmlPullParser.TEXT:

                    String text = parser.getText();

                    if (!StringUtil.isEmptyOrWhitespace(text)) {
                        handleText(text, tag, attributes, gamesBackup, game, playerScore);
                    }
                    break;
                }

                parserEvent = parser.next();
            }
        } catch (XmlPullParserException e) {
            log.e(e, "unexpected");
        } catch (IOException e) {
            log.e(e, "unexpected");
        }

        // return de-serialized game backup
        return gamesBackup;
    }

    private static void handleText(String text, Tag tag, Map<String, String> attributes, GamesBackup gamesBackup,
            Game game, PlayerScore playerScore) {

        switch (tag) {
            case gameCount:
                gamesBackup.setGameCount(Integer.parseInt(text));
                break;
            case backupFilename:
                gamesBackup.setFilename(text);
                break;
            case version:
                gamesBackup.setVersion(Integer.parseInt(text));
                break;
            case automatic:
                gamesBackup.setAutomatic(Boolean.parseBoolean(text));
                break;            
            case dateBackupSaved:
                gamesBackup.setDateSaved(Long.parseLong(text));
                break;
            case dateGameSaved:
                game.setDateSaved(Long.parseLong(text));
                break;
            case dateGameStarted:
                game.setDateStarted(Long.parseLong(text));
                break;
            case gameName:
                game.setName(getTextOrNullOrEmpty(attributes, text));
                break;
            case playerName:
                playerScore.setName(getTextOrNullOrEmpty(attributes, text));
                break;
            case playerNumber:
                playerScore.setPlayerNumber(Integer.parseInt(text));
                break;
            case history:
                playerScore.setHistory(CollectionUtil.stringsToInts(StringUtil.split(
                        getTextOrNullOrEmpty(attributes, text), ',')));
                break;
            case score:
                playerScore.setScore(Long.parseLong(text));
                break;
            case lastUpdate:
                playerScore.setLastUpdate(Long.parseLong(text));
                break;
        }

    }

    private static String getTextOrNullOrEmpty(Map<String, String> attributes, String text) {
        if (Boolean.parseBoolean(attributes.get(ATTRIBUTE_NULL))) {
            return null;
        } else if (Boolean.parseBoolean(attributes.get(ATTRIBUTE_EMPTY))) {
            return "";
        }
        return text;
    }

    public static String serialize(GamesBackup gamesBackup) {
        String rawXml = serializeAsRawXml(gamesBackup);

        // pretty-print the xml
        return XmlHelper.prettyPrint(rawXml);
    }

    private static String serializeAsRawXml(GamesBackup gamesBackup) {
        XmlSerializer serializer = Xml.newSerializer();

        StringWriter writer = new StringWriter();
        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.startTag("", Tag.GamesBackup.name());
            addTag(serializer, Tag.gameCount, gamesBackup.getGameCount());
            addTag(serializer, Tag.version, gamesBackup.getVersion());
            addTag(serializer, Tag.automatic, gamesBackup.isAutomatic());
            addTag(serializer, Tag.backupFilename, gamesBackup.getFilename());
            addTag(serializer, Tag.dateBackupSaved, gamesBackup.getDateSaved());
            serializer.startTag("", Tag.Games.name());
            for (Game game : gamesBackup.getGames()) {
                serializer.startTag("", Tag.Game.name());
                addTag(serializer, Tag.dateGameSaved, game.getDateSaved());
                addTag(serializer, Tag.dateGameStarted, game.getDateStarted());
                addTag(serializer, Tag.gameName, game.getName());

                serializer.startTag("", Tag.PlayerScores.name());
                for (PlayerScore playerScore : game.getPlayerScores()) {
                    serializer.startTag("", Tag.PlayerScore.name());

                    addTag(serializer, Tag.playerName, playerScore.getName());
                    addTag(serializer, Tag.score, playerScore.getScore());
                    addTag(serializer, Tag.playerNumber, playerScore.getPlayerNumber());
                    addTag(serializer, Tag.history, TextUtils.join(",", playerScore.getHistory()));
                    addTag(serializer, Tag.lastUpdate, Long.toString(playerScore.getLastUpdate()));

                    serializer.endTag("", Tag.PlayerScore.name());
                }
                serializer.endTag("", Tag.PlayerScores.name());
                serializer.endTag("", Tag.Game.name());
            }
            serializer.endTag("", Tag.Games.name());
            serializer.endTag("", Tag.GamesBackup.name());

            serializer.endDocument();
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convenience method for adding tags
     * 
     * @param serializer
     * @param tag
     * @param value
     * @throws IOException
     */
    private static void addTag(XmlSerializer serializer, Tag tag, Object value) throws IOException {
        serializer.startTag("", tag.name());
        if (value == null) {
            // explicitly mark nulls with an attribute
            serializer.attribute("", ATTRIBUTE_NULL, Boolean.TRUE.toString());
        } else if (value.equals("")) {
            // explicitly mark empty strings
            serializer.attribute("", ATTRIBUTE_EMPTY, Boolean.TRUE.toString());
        }
        serializer.text(String.valueOf("".equals(value) ? null : value));
        serializer.endTag("", tag.name());
    }

    private static Map<String, String> getAttributes(XmlPullParser parser) {
        Map<String, String> attrs = new HashMap<String, String>();
        for (int i = 0; i < parser.getAttributeCount(); i++) {
            attrs.put(parser.getAttributeName(i), parser.getAttributeValue(i));
        }
        return attrs;
    }
}
