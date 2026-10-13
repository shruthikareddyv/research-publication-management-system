package org.example.ui;

import java.awt.*;

/** A FlowLayout that calculates a wrapped preferred height, preventing action buttons from being clipped. */
public class WrapLayout extends FlowLayout {
    public WrapLayout() { super(); }
    public WrapLayout(int align) { super(align); }
    public WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        return layoutSize(target, true);
    }

    @Override
    public Dimension minimumLayoutSize(Container target) {
        Dimension minimum = layoutSize(target, false);
        minimum.width -= (getHgap() + 1);
        return minimum;
    }

    private Dimension layoutSize(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            int targetWidth = target.getSize().width;
            if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;
            Insets insets = target.getInsets();
            int horizontalInsets = insets.left + insets.right + getHgap() * 2;
            int maxWidth = targetWidth - horizontalInsets;
            Dimension dim = new Dimension(0, 0);
            int rowWidth = 0;
            int rowHeight = 0;
            for (Component component : target.getComponents()) {
                if (!component.isVisible()) continue;
                Dimension d = preferred ? component.getPreferredSize() : component.getMinimumSize();
                if (rowWidth > 0 && rowWidth + getHgap() + d.width > maxWidth) {
                    dim.width = Math.max(dim.width, rowWidth);
                    dim.height += rowHeight + getVgap();
                    rowWidth = 0;
                    rowHeight = 0;
                }
                if (rowWidth > 0) rowWidth += getHgap();
                rowWidth += d.width;
                rowHeight = Math.max(rowHeight, d.height);
            }
            dim.width = Math.max(dim.width, rowWidth);
            dim.height += rowHeight;
            dim.width += horizontalInsets;
            dim.height += insets.top + insets.bottom + getVgap() * 2;
            return dim;
        }
    }
}
