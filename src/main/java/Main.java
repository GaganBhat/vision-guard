import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.Image;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;


public class Main {

	public static final String collectionID = "vision-guard-faces";

	public static void main(String[] args) throws IOException {
		AmazonRekognition amazonRekognition = AmazonRekognitionClientBuilder.defaultClient();

		Image testImage = new Image().withBytes(getByteBuffer("testpic.jpg"));
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
