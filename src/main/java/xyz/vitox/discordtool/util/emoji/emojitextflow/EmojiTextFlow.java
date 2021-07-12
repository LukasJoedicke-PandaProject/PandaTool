// Copyright (c) 2020, Pavlo Buidenkov. All rights reserved.
// Use of this source code is governed by a BSD 3-Clause License
// that can be found in the LICENSE file.

package xyz.vitox.discordtool.util.emoji.emojitextflow;

import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

import java.util.Queue;

/**
 * Created by Pavlo (Paul) Buidenkov on 2020-07-27
 * Pavlo Buidenkov github page: https://github.com/pavlobu
 */

public class EmojiTextFlow extends TextFlow {

    private EmojiTextFlowParameters parameters;

    public EmojiTextFlow() {
        initializeDefaultParametersObject();
    }

    private void initializeDefaultParametersObject() {
        parameters = new EmojiTextFlowParameters();
        parameters.setEmojiScaleFactor(1D);
        parameters.setTextAlignment(TextAlignment.CENTER);
        parameters.setFont(Font.font("System", FontWeight.NORMAL, 35));
        parameters.setTextColor(Color.BLACK);
    }

    public EmojiTextFlow(EmojiTextFlowParameters parameters) {
        this.parameters = parameters;
        if (parameters.getTextAlignment() != null) {
            this.setTextAlignment(parameters.getTextAlignment());
        }
    }

    public void parseAndAppend(String message) {
        Queue<Object> obs = EmojiParser.getInstance().toEmojiAndText(message);
        while (!obs.isEmpty()) {
            Object ob = obs.poll();
            if (ob instanceof String) {
                addTextNode((String) ob);
                continue;
            }
            if (ob instanceof Emoji) {
                Emoji emoji = (Emoji) ob;
                try {
                    addEmojiImageNode(createEmojiImageNode(emoji));
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    addTextNode(emoji.getUnicode());
                }
            }
        }

    }

    private ImageView createEmojiImageNode(Emoji emoji) throws NullPointerException {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(parameters.getEmojiFitWidth());
        imageView.setFitHeight(parameters.getEmojiFitHeight());
        imageView.setImage(EmojiImageCache.getInstance().getImage(getEmojiImagePath(emoji.getHex())));
        return imageView;
    }

    private void addTextNode(String text) {
        Text textNode = new Text();
        textNode.setText(text);
        textNode.setFont(parameters.getFont());
        if (parameters.getTextColor() != null) {
            textNode.setFill(parameters.getTextColor());
        }

        this.getChildren().add(textNode);
    }

    private void addEmojiImageNode(ImageView emojiImageNode) {
        this.getChildren().add(emojiImageNode);
    }

    private String getEmojiImagePath(String hexStr) throws NullPointerException {
        return this.getClass().getClassLoader().getResource("emoji_images/" + hexStr + ".png").toExternalForm();
    }
}
