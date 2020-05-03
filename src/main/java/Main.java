import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import com.amazonaws.services.rekognition.model.Image;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


public class Main extends Application {

	public static final String collectionID = "vision-guard-faces";

	public static void main(String[] args) throws IOException {
		launch(args);
	}


	@Override
	public void start(Stage primaryStage) {
		AmazonRekognition amazonRekognition = AmazonRekognitionClientBuilder.defaultClient();

		FileChooser fileChooser = new FileChooser();
		AtomicReference<String> filePath = new AtomicReference<>("");
		TextField nameField = new TextField();
		nameField.setPromptText("Enter Name Here");

		Button fileButton = new Button();
		fileButton.setText("Choose Image");
		fileButton.setOnAction(click -> {
			File file = fileChooser.showOpenDialog(primaryStage);
			if (file != null)
				filePath.set(file.getAbsolutePath());
		});

		Button indexFace = new Button();
		indexFace.setText("Register Face");
		indexFace.setOnAction(click -> {
			System.out.println("Indexing Face!");
			System.out.println("Name Field = " + nameField.getText().replaceAll("\\s", ""));
			indexFace(amazonRekognition,filePath.toString(), nameField.getText().replaceAll("\\s", ""));
		});

		Button searchFace = new Button();
		searchFace.setText("Search Face");
		searchFace.setOnAction(click -> {
			System.out.println("Searching for Face!");
			searchFace(amazonRekognition,filePath.toString());
		});

		StackPane root = new StackPane();
		VBox vbox = new VBox(5); // 5 is the spacing between elements in the VBox
		vbox.getChildren().addAll(fileButton, nameField, indexFace, searchFace);
		StackPane.setAlignment(vbox, Pos.CENTER);
		root.getChildren().add(vbox);

		Scene scene = new Scene(root, 300, 250);

		primaryStage.setTitle("Vision Guard");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void indexFace(AmazonRekognition rekognition, String filePath, String fileName) {
		try {
			Image image = new Image().withBytes(getByteBuffer(filePath));

			IndexFacesRequest indexFacesRequest = new IndexFacesRequest()
					.withImage(image)
					.withQualityFilter(QualityFilter.AUTO)
					.withMaxFaces(1)
					.withCollectionId(collectionID)
					.withExternalImageId(fileName)
					.withDetectionAttributes("DEFAULT");

			IndexFacesResult indexFacesResult = rekognition.indexFaces(indexFacesRequest);

			System.out.println("Results for " + fileName);
			System.out.println("Faces indexed:");
			List<FaceRecord> faceRecords = indexFacesResult.getFaceRecords();
			for (FaceRecord faceRecord : faceRecords) {
				System.out.println("  Face ID: " + faceRecord.getFace().getFaceId());
				System.out.println("  Location:" + faceRecord.getFaceDetail().getBoundingBox().toString());
			}

			List<UnindexedFace> unindexedFaces = indexFacesResult.getUnindexedFaces();
			System.out.println("Faces not indexed:");
			for (UnindexedFace unindexedFace : unindexedFaces) {
				System.out.println("  Location:" + unindexedFace.getFaceDetail().getBoundingBox().toString());
				System.out.println("  Reasons:");
				for (String reason : unindexedFace.getReasons()) {
					System.out.println("   " + reason);
				}
			}
		} catch (Exception e) {
			System.out.println("File Error!");
			e.printStackTrace();
		}
	}

	public static String searchFace(AmazonRekognition rekognition, String filePath){
		Image image = new Image().withBytes(getByteBuffer(filePath));

		SearchFacesByImageRequest searchFacesByImageRequest = new SearchFacesByImageRequest()
				.withImage(image)
				.withQualityFilter(QualityFilter.AUTO)
				.withMaxFaces(1)
				.withCollectionId(collectionID);

		SearchFacesByImageResult searchFaceResult = rekognition.searchFacesByImage(searchFacesByImageRequest);

		if(searchFaceResult.getFaceMatches().isEmpty()) {
			return "No Faces Found!";
		}

		StringBuilder result = new StringBuilder();
		for(FaceMatch faceMatch : searchFaceResult.getFaceMatches())
			result.append("Matched Face Name ").append(faceMatch.getFace().getExternalImageId()).append(" at ").
					append(faceMatch.getSimilarity()).append("%").append(" confident.\n");

		System.out.println(result.toString());

		return result.toString();
	}

	public static ByteBuffer getByteBuffer(String fileName) {
		try {
			BufferedImage originalImage = ImageIO.read(new File(fileName));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(originalImage, "jpg", baos);
			baos.flush();
			byte[] imageInByte = baos.toByteArray();
			baos.close();
			return ByteBuffer.wrap(imageInByte);
		} catch (Exception e){
			System.out.println("File not found!");
			e.printStackTrace();
		}
		return null;
	}
}
