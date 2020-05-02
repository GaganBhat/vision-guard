import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import com.amazonaws.services.rekognition.model.Image;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamException;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;


public class Main extends Application {

	public static final String collectionID = "vision-guard-faces";

	public static void main(String[] args) throws IOException {
		AmazonRekognition amazonRekognition = AmazonRekognitionClientBuilder.defaultClient();
		launch(args);
	}


	@Override
	public void start(Stage primaryStage) {
		Button btn = new Button();
		btn.setText("Say 'Hello World'");
		btn.setOnAction(click -> {
			System.out.println("Button Clicked!");
		});

		StackPane root = new StackPane();
		root.getChildren().add(btn);

		Scene scene = new Scene(root, 300, 250);

		primaryStage.setTitle("Hello World!");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void indexFace(AmazonRekognition rekognition, String filePath) throws IOException {
		Image testImage = new Image().withBytes(getByteBuffer(filePath));

		IndexFacesRequest indexFacesRequest = new IndexFacesRequest()
				.withImage(testImage)
				.withQualityFilter(QualityFilter.AUTO)
				.withMaxFaces(1)
				.withCollectionId(collectionID)
				.withExternalImageId(filePath)
				.withDetectionAttributes("DEFAULT");

		IndexFacesResult indexFacesResult = rekognition.indexFaces(indexFacesRequest);

		System.out.println("Results for " +  filePath);
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
	}

	public static ByteBuffer getByteBuffer(String fileName) throws IOException {
		BufferedImage originalImage = ImageIO.read(new File(fileName));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write( originalImage, "jpg", baos );
		baos.flush();
		byte[] imageInByte = baos.toByteArray();
		baos.close();
		return ByteBuffer.wrap(imageInByte);
	}
}
