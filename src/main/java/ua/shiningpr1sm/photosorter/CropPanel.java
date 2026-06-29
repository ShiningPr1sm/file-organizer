package ua.shiningpr1sm.photosorter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

public class CropPanel extends JPanel {
    private BufferedImage originalImage;
    private BufferedImage scaledImage;
    private double scaleFactor;

    private Rectangle originalCropRectangle;
    private Rectangle scaledCropRectangle;

    private Point startPointScaled;
    private boolean dragging;

    private static final int DIALOG_CHROME_HEIGHT = 100;
    private static final int DIALOG_CHROME_WIDTH = 20;

    public CropPanel(BufferedImage image) {
        this.originalImage = image;

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int maxWidth = (int)(screenSize.width * 0.9) - DIALOG_CHROME_WIDTH;
        int maxHeight = (int)(screenSize.height * 0.9) - DIALOG_CHROME_HEIGHT;

        double scaleX = (double) maxWidth / originalImage.getWidth();
        double scaleY = (double) maxHeight / originalImage.getHeight();
        this.scaleFactor = Math.min(1.0, Math.min(scaleX, scaleY));

        int scaledWidth = (int) (originalImage.getWidth() * scaleFactor);
        int scaledHeight = (int) (originalImage.getHeight() * scaleFactor);

        this.scaledImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = this.scaledImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();

        setPreferredSize(new Dimension(scaledWidth, scaledHeight));
        setMinimumSize(new Dimension(scaledWidth, scaledHeight));
        setMaximumSize(new Dimension(scaledWidth, scaledHeight));
        setBackground(Color.DARK_GRAY);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int imgOffsetX = (getWidth() - scaledImage.getWidth()) / 2;
                int imgOffsetY = (getHeight() - scaledImage.getHeight()) / 2;
                int clickX = e.getX() - imgOffsetX;
                int clickY = e.getY() - imgOffsetY;

                if (clickX >= 0 && clickX < scaledImage.getWidth() &&
                        clickY >= 0 && clickY < scaledImage.getHeight()) {
                    startPointScaled = new Point(clickX, clickY);
                    dragging = true;
                    originalCropRectangle = null;
                    scaledCropRectangle = null;
                    repaint();
                } else {
                    startPointScaled = null;
                    dragging = false;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragging = false;
                if (startPointScaled != null) {
                    int imgOffsetX = (getWidth() - scaledImage.getWidth()) / 2;
                    int imgOffsetY = (getHeight() - scaledImage.getHeight()) / 2;
                    int currentXScaled = e.getX() - imgOffsetX;
                    int currentYScaled = e.getY() - imgOffsetY;

                    currentXScaled = Math.max(0, Math.min(currentXScaled, scaledImage.getWidth()));
                    currentYScaled = Math.max(0, Math.min(currentYScaled, scaledImage.getHeight()));

                    int xScaled = Math.min(startPointScaled.x, currentXScaled);
                    int yScaled = Math.min(startPointScaled.y, currentYScaled);
                    int widthScaled = Math.abs(startPointScaled.x - currentXScaled);
                    int heightScaled = Math.abs(startPointScaled.y - currentYScaled);

                    if (widthScaled > 0 && heightScaled > 0) {
                        scaledCropRectangle = new Rectangle(xScaled, yScaled, widthScaled, heightScaled);
                        originalCropRectangle = scaledToOriginal(scaledCropRectangle);
                    } else {
                        originalCropRectangle = null;
                        scaledCropRectangle = null;
                    }
                    startPointScaled = null;
                    repaint();
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragging && startPointScaled != null) {
                    int imgOffsetX = (getWidth() - scaledImage.getWidth()) / 2;
                    int imgOffsetY = (getHeight() - scaledImage.getHeight()) / 2;
                    int currentXScaled = e.getX() - imgOffsetX;
                    int currentYScaled = e.getY() - imgOffsetY;

                    currentXScaled = Math.max(0, Math.min(currentXScaled, scaledImage.getWidth()));
                    currentYScaled = Math.max(0, Math.min(currentYScaled, scaledImage.getHeight()));

                    int xDraw = Math.min(startPointScaled.x, currentXScaled);
                    int yDraw = Math.min(startPointScaled.y, currentYScaled);
                    int widthDraw = Math.abs(startPointScaled.x - currentXScaled);
                    int heightDraw = Math.abs(startPointScaled.y - currentYScaled);

                    scaledCropRectangle = new Rectangle(xDraw, yDraw, widthDraw, heightDraw);
                    repaint();
                }
            }
        });
    }

    /** Преобразует прямоугольник из координат масштабированного изображения в оригинальные */
    private Rectangle scaledToOriginal(Rectangle r) {
        int x = (int) (r.x / scaleFactor);
        int y = (int) (r.y / scaleFactor);
        int w = (int) (r.width / scaleFactor);
        int h = (int) (r.height / scaleFactor);

        x = Math.max(0, Math.min(x, originalImage.getWidth() - 1));
        y = Math.max(0, Math.min(y, originalImage.getHeight() - 1));
        w = Math.min(w, originalImage.getWidth() - x);
        h = Math.min(h, originalImage.getHeight() - y);

        return new Rectangle(x, y, w, h);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (scaledImage == null) return;

        int imgOffsetX = (getWidth() - scaledImage.getWidth()) / 2;
        int imgOffsetY = (getHeight() - scaledImage.getHeight()) / 2;
        g.drawImage(scaledImage, imgOffsetX, imgOffsetY, this);

        if (scaledCropRectangle != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.translate(imgOffsetX, imgOffsetY);

            g2d.setColor(new Color(0, 0, 0, 120));
            int sw = scaledImage.getWidth(), sh = scaledImage.getHeight();
            Rectangle r = scaledCropRectangle;
            g2d.fillRect(0, 0, sw, r.y);
            g2d.fillRect(0, r.y + r.height, sw, sh - (r.y + r.height));
            g2d.fillRect(0, r.y, r.x, r.height);
            g2d.fillRect(r.x + r.width, r.y, sw - (r.x + r.width), r.height);

            g2d.setColor(new Color(255, 50, 50));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(r.x, r.y, r.width, r.height);

            g2d.dispose();
        }
    }

    /**
     * Создаёт диалог с этой панелью. Размер окна подгоняется под картинку,
     * но никогда не выходит за рамки экрана.
     */
    public static CropPanel showDialog(Component parent, BufferedImage image) {
        CropPanel cropPanel = new CropPanel(image);

        JOptionPane optionPane = new JOptionPane(
                cropPanel,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION
        );
        JDialog dialog = optionPane.createDialog(parent, "Обрезка изображения");
        dialog.setResizable(false);
        dialog.pack();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension dlgSize = dialog.getSize();
        if (dlgSize.width > screenSize.width || dlgSize.height > screenSize.height) {
            dialog.setSize(
                    Math.min(dlgSize.width, screenSize.width),
                    Math.min(dlgSize.height, screenSize.height)
            );
        }
        dialog.setLocationRelativeTo(parent);

        dialog.setVisible(true);

        Object value = optionPane.getValue();
        if (value instanceof Integer && (Integer) value == JOptionPane.OK_OPTION) {
            return cropPanel;
        }
        return null;
    }

    public BufferedImage getCroppedImage() {
        if (originalCropRectangle == null || originalCropRectangle.isEmpty() || originalImage == null) {
            return originalImage;
        }
        try {
            int x = Math.max(0, Math.min(originalCropRectangle.x, originalImage.getWidth() - 1));
            int y = Math.max(0, Math.min(originalCropRectangle.y, originalImage.getHeight() - 1));
            int w = Math.min(originalCropRectangle.width, originalImage.getWidth() - x);
            int h = Math.min(originalCropRectangle.height, originalImage.getHeight() - y);

            if (w <= 0 || h <= 0) return originalImage;

            return originalImage.getSubimage(x, y, w, h);
        } catch (Exception e) {
            System.err.println("Error getting cropped image: " + e.getMessage());
            return originalImage;
        }
    }
}