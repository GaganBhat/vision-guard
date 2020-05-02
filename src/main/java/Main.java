import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;


public class Main {

	public static final String collectionID = "vision-guard-faces";

	public static void main(String[] args) throws IOException {
		AmazonRekognition amazonRekognition = AmazonRekognitionClientBuilder.defaultClient();

		Image testImage = new Image().withBytes(getByteBuffer("testpic.jpg"));

		IndexFacesRequest indexFacesRequest = new IndexFacesRequest()
				.withImage(testImage)
				.withQualityFilter(QualityFilter.AUTO)
				.withMaxFaces(1)
				.withCollectionId(collectionID)
				.withExternalImageId("testpic.jpg")
				.withDetectionAttributes("DEFAULT");

		IndexFacesResult indexFacesResult = amazonRekognition.indexFaces(indexFacesRequest);

		System.out.println("Results for " + "testpic.jpg");
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
