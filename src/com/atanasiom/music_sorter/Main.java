package com.atanasiom.music_sorter;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

/**
 * @author Michael Atanasio
 *
 */
public class Main extends Application {

	/**
	 * The width of the GUI
	 */
	private final int WIDTH = 400;
	/**
	 * The height of the GUI
	 */
	private final int HEIGHT = 200;

	// I am using two ArrayLists for this program. This is because the AudioFile
	// can take a normal File object to create itself. I also figured this was
	// easier than constantly creating new AudioFile objects every time I wanted
	// to read Tag information

	/**
	 * The ArrayList that will contain each song that needs to be sorted. These
	 * are stored as standard File objects.
	 */
	private ArrayList<File> fileList = new ArrayList<File>();
	/**
	 * The ArrayList that will contain each song that needs to be sorted. These
	 * are stored as AudioFile objects.
	 */
	private ArrayList<AudioFile> audioList = new ArrayList<AudioFile>();

	private TextField pathField = new TextField();
	private Button browseButton = new Button("Browse...");
	private Button fixButton = new Button("Fix my music");
	private Text directoryText = new Text();
	private Text finishedText = new Text();
	private DirectoryChooser dc = new DirectoryChooser();

	@Override
	public void start(Stage primaryStage) throws Exception {

		pathField.setPromptText("Enter path here...");
		pathField.setMinWidth(200);
		pathField.setOnAction(e -> {
			finishedText.setText("");
			fixMusic(pathField.getText());
		});
		pathField.setOnKeyReleased(e -> {
			checkDirectory(pathField.getText());
		});

		browseButton.setOnAction(e -> {
			File tempFile = new File(pathField.getText());
			dc.setInitialDirectory(tempFile.isDirectory() ? tempFile : null);
			File newFile = dc.showDialog(primaryStage);
			if (newFile != null) {
				pathField.setText(newFile.getAbsolutePath());
				checkDirectory(pathField.getText());
			}
		});

		fixButton.setOnAction(e -> {
			finishedText.setText("");
			fixMusic(pathField.getText());
		});

		GridPane pane = new GridPane();

		pane.setHgap(10);
		pane.setVgap(10);
		pane.setPadding(new Insets(10, 10, 10, 10));

		pane.add(new Text("Enter path of music:"), 0, 0);
		pane.add(pathField, 1, 0);
		pane.add(browseButton, 2, 0);
		pane.add(fixButton, 1, 1);
		pane.add(directoryText, 1, 2);
		pane.add(finishedText, 1, 3);

		pane.setOnMouseClicked(e -> {
			pane.requestFocus();
		});

		Scene scene = new Scene(pane, WIDTH, HEIGHT);
		primaryStage.setTitle("ListViewDemo");
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.show();
		pane.requestFocus();

	}

	/**
	 * The main method for fixing the music.<br>
	 * <br>
	 * Calls:<br>
	 * {@link #getFileList(File)}<br>
	 * {@link #moveFiles()}
	 * 
	 * @param path
	 *            the main path where the music lies
	 */
	private void fixMusic(String path) {

		fileList.clear();
		audioList.clear();

		finishedText.setText("");

		File mainDirectory = new File(path.trim().replaceAll("\\\\", "\\\\\\\\"));

		if (checkDirectory(path)) {
			getFileList(mainDirectory);
			moveFiles();

			finishedText.setFill(Color.GREEN);
			finishedText.setText("Done!");
		}

	}

	/**
	 * 
	 */
	private void moveFiles() {
		for (int idx = 0; idx < audioList.size(); idx++) {
			File currFile = fileList.get(idx);
			AudioFile currAudio = audioList.get(idx);
			Tag currTag = currAudio.getTag();
		}

		// TODO FOR TESTING PURPOSES
		renameFile(0, audioList.get(0).getTag().getFirst(FieldKey.TITLE),
				audioList.get(0).getTag().getFirst(FieldKey.TRACK));
	}

	/**
	 * Renames the file given by the index number based on it's title and track
	 * number <br>
	 * <br>
	 * Uses the following format:<br>
	 * <br>
	 * {trackNumber} - {title}
	 * 
	 * @param index
	 *            the index of the file within the fileList
	 * @param title
	 *            the title of the song
	 * @param track
	 *            the track number of the song
	 * @return {@code true} if renaming is successful, or {@code false} if
	 *         renaming is unsuccessful
	 */
	private boolean renameFile(int index, String title, String track) {
		File currFile = fileList.get(index);

		String path = currFile.getAbsolutePath();
		path = path.substring(0, path.lastIndexOf("\\") + 1);

		System.out.println(path);

		int trackNum = 0;

		if (!track.trim().equals(""))
			trackNum = Integer.parseInt(track.trim());

		DecimalFormat df = new DecimalFormat("00");
		StringBuilder sb = new StringBuilder();

		sb.append(path.replaceAll("\\\\", "\\\\\\\\"));
		sb.append(df.format(trackNum));
		sb.append(" - ");
		sb.append(title.trim());
		sb.append(getExtension(currFile.getName()));

		System.out.println(sb.toString());
		File newFile = new File(sb.toString());

		boolean success = currFile.renameTo(newFile);

		try {
			audioList.set(index, AudioFileIO.read(newFile));
			fileList.set(index, newFile);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return success;
	}

	/**
	 * @param mainDirectory
	 */
	private void getFileList(File mainDirectory) {
		for (File f : mainDirectory.listFiles()) {
			if (f.isDirectory())
				getFileList(f);
			else {
				String extension = getExtension(f.getName());
				if (extension.equalsIgnoreCase(".mp3") || extension.equalsIgnoreCase(".m4a")
						|| extension.equalsIgnoreCase(".flac")) {
					try {
						audioList.add(AudioFileIO.read(f));
						fileList.add(f);
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
				}
			}
		}
	}

	private String getExtension(String file) {
		return file.substring(file.lastIndexOf("."), file.length());
	}

	/**
	 * Checks if the given file path is a directory and displays the required
	 * text
	 * 
	 * @param path
	 *            the path of the directory
	 * @return {@code true} if the directory is good, or {@code false} if the
	 *         directory is bad
	 */
	private boolean checkDirectory(String path) {
		if (!new File(path.trim().replaceAll("\\\\", "\\\\\\\\")).isDirectory()) {
			directoryText.setFill(Color.RED);
			directoryText.setText("Bad Directory");
			return false;
		} else {
			directoryText.setFill(Color.GREEN);
			directoryText.setText("Good Directory");
			return true;
		}
	}

	public static void main(String... args) {
		launch(args);
	}

}