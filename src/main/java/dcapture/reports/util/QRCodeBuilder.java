package dcapture.reports.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Hashtable;

public class QRCodeBuilder {
    private int width, height;

    public QRCodeBuilder() {
        this.width = 128;
        this.height = 128;
    }

    public static void main(String[] args) {
        Path path = new QRCodeBuilder().build("upi://pay?pa=vethiyallabs@sbi&pn=VETHIYAL%20LABS%20AND%20SCIENTIFICS&mc=5169&tr=&tn=&am=&cu=INR&url=&mode=02&purpose=00&orgid=159002&sign=MEYCIQDrWxSvav98YldsktCaaUuUUWm9ytGVAIgpy4Oh+dfV6AIhAKFp9O11+iLUrvS5Ardy1VCoFXTorRGQyu2OS1ALocQR");
        System.out.println(path);
    }

    public QRCodeBuilder setWidth(int width) {
        this.width = width;
        return QRCodeBuilder.this;
    }

    public QRCodeBuilder setHeight(int height) {
        this.height = height;
        return QRCodeBuilder.this;
    }

    public Path build(String data) {
        Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Path imagePath;
        try {
            BitMatrix byteMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height, hintMap);
            BufferedImage image = new BufferedImage(byteMatrix.getWidth(), byteMatrix.getHeight(), BufferedImage.TYPE_INT_RGB);
            image.createGraphics();
            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, byteMatrix.getWidth(), byteMatrix.getHeight());
            graphics.setColor(Color.BLACK);
            for (int i = 0; i < byteMatrix.getWidth(); i++) {
                for (int j = 0; j < byteMatrix.getHeight(); j++) {
                    if (byteMatrix.get(i, j)) {
                        graphics.fillRect(i, j, 1, 1);
                    }
                }
            }
            imagePath = IOFolderFileUtil.getTempRandomFile("jpg");
            ImageIO.write(image, "jpg", imagePath.toFile());
        } catch (WriterException | IOException ex) {
            throw new RuntimeException(ex);
        }
        return imagePath;
    }
}
