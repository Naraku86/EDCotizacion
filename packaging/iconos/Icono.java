import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/** Dibuja el icono de EDCotizacion: hoja de cotización sobre un cuadro redondeado. */
public class Icono {
    public static void main(String[] a) throws Exception {
        for (String s : a[1].split(",")) {
            int n = Integer.parseInt(s);
            ImageIO.write(dibujar(n), "png", new File(a[0], "icono-" + n + ".png"));
        }
    }

    static BufferedImage dibujar(int n) {
        BufferedImage img = new BufferedImage(n, n, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        double u = n / 100.0;
        // fondo
        g.setColor(new Color(0x184E62));
        g.fill(new RoundRectangle2D.Double(4 * u, 4 * u, 92 * u, 92 * u, 22 * u, 22 * u));
        // hoja con esquina doblada
        Path2D hoja = new Path2D.Double();
        hoja.moveTo(26 * u, 16 * u); hoja.lineTo(62 * u, 16 * u); hoja.lineTo(76 * u, 30 * u);
        hoja.lineTo(76 * u, 84 * u); hoja.lineTo(26 * u, 84 * u); hoja.closePath();
        g.setColor(Color.WHITE);
        g.fill(hoja);
        Path2D doblez = new Path2D.Double();
        doblez.moveTo(62 * u, 16 * u); doblez.lineTo(62 * u, 30 * u); doblez.lineTo(76 * u, 30 * u); doblez.closePath();
        g.setColor(new Color(0x79DED0));
        g.fill(doblez);
        // renglones
        g.setColor(new Color(0xB9CCD2));
        double grosor = Math.max(1, 5 * u);
        for (int i = 0; i < 3; i++) {
            g.fill(new RoundRectangle2D.Double(33 * u, (36 + i * 10) * u, (i == 0 ? 24 : 36) * u, grosor, grosor, grosor));
        }
        // total
        g.setColor(new Color(0x087F73));
        g.fill(new RoundRectangle2D.Double(33 * u, 68 * u, 36 * u, 8 * u, 3 * u, 3 * u));
        g.dispose();
        return img;
    }
}
