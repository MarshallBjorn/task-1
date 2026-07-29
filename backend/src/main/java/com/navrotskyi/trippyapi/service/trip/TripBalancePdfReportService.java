package com.navrotskyi.trippyapi.service.trip;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

import com.navrotskyi.trippyapi.domain.TripEvent;
import com.navrotskyi.trippyapi.dto.trip.ParticipantBalanceDto;
import com.navrotskyi.trippyapi.dto.trip.SettlementDto;
import com.navrotskyi.trippyapi.dto.trip.TripBalanceResponse;

/**
 * Renderuje estetyczny raport PDF rozliczeń wycieczki na podstawie
 * {@link TripBalanceResponse} (czysta warstwa prezentacji — bez dostępu do DB).
 *
 * <p>Layout:</p>
 * <ul>
 *   <li>kolorowy nagłówek (banner) z nazwą wycieczki, zakresem dat i walutą,</li>
 *   <li>kafelek podsumowania (łączne wydatki współdzielone),</li>
 *   <li>tabela bilansów uczestników (zielony = ma do otrzymania, czerwony = winien),</li>
 *   <li>tabela przelewów "kto komu płaci",</li>
 *   <li>stopka z datą wygenerowania i numeracją stron.</li>
 * </ul>
 */
@Service
public class TripBalancePdfReportService {

    // Paleta kolorów
    private static final Color BRAND      = new Color(0x1E, 0x88, 0xE5); // niebieski
    private static final Color BRAND_DARK = new Color(0x0D, 0x47, 0xA1);
    private static final Color BG_SOFT    = new Color(0xF1, 0xF5, 0xF9); // jasne tło wierszy
    private static final Color POSITIVE   = new Color(0x2E, 0x7D, 0x32); // zielony
    private static final Color NEGATIVE   = new Color(0xC6, 0x28, 0x28); // czerwony
    private static final Color MUTED      = new Color(0x6B, 0x72, 0x80);
    private static final Color BORDER     = new Color(0xD1, 0xD5, 0xDB);

    private static final Font F_TITLE   = font(22, Font.BOLD, Color.WHITE);
    private static final Font F_SUBTITLE= font(11, Font.NORMAL, new Color(0xE3, 0xF2, 0xFD));
    private static final Font F_SECTION = font(14, Font.BOLD, BRAND_DARK);
    private static final Font F_TH      = font(10, Font.BOLD, Color.WHITE);
    private static final Font F_TD      = font(10, Font.NORMAL, new Color(0x1F, 0x29, 0x37));
    private static final Font F_TD_B    = font(10, Font.BOLD, new Color(0x1F, 0x29, 0x37));
    private static final Font F_KPI     = font(20, Font.BOLD, BRAND_DARK);
    private static final Font F_KPI_LBL = font(9, Font.NORMAL, MUTED);
    private static final Font F_FOOTER  = font(8, Font.NORMAL, MUTED);

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static Font font(float size, int style, Color color) {
        Font f = new Font(Font.HELVETICA, size, style);
        f.setColor(color);
        return f;
    }

    /**
     * @param trip   encja wycieczki (nazwa, daty) — tylko do nagłówka
     * @param data   wyliczone bilanse i przelewy
     * @return bajty gotowego dokumentu PDF
     */
    public byte[] generate(TripEvent trip, TripBalanceResponse data) {
        Document doc = new Document(PageSize.A4, 40, 40, 90, 50);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            writer.setPageEvent(new HeaderFooter(trip, data.currencyCode()));
            doc.open();

            doc.add(summaryCard(data.totalSharedExpenses(), data.currencyCode(),
                    data.balances().size(), data.settlements().size()));
            doc.add(spacer(18));

            doc.add(section("Bilanse uczestnikow"));
            doc.add(spacer(6));
            doc.add(balancesTable(data));
            doc.add(spacer(20));

            doc.add(section("Lista przelewow (zminimalizowana)"));
            doc.add(spacer(6));
            doc.add(settlementsTable(data));

            doc.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new IllegalStateException("Blad generowania PDF rozliczen", e);
        }
    }

    // ---------- sekcje ----------

    private Paragraph spacer(float h) {
        Paragraph p = new Paragraph(" ");
        p.setLeading(h);
        return p;
    }

    private Paragraph section(String title) {
        Paragraph p = new Paragraph(title, F_SECTION);
        p.setSpacingBefore(4);
        return p;
    }

    private PdfPTable summaryCard(BigDecimal total, String currency, int people, int transfers) {
        PdfPTable t = new PdfPTable(3);
        t.setWidthPercentage(100);
        t.addCell(kpiCell(money(total) + " " + currency, "Wydatki wspoldzielone"));
        t.addCell(kpiCell(String.valueOf(people), "Uczestnikow"));
        t.addCell(kpiCell(String.valueOf(transfers), "Przelewow do wykonania"));
        return t;
    }

    private PdfPCell kpiCell(String value, String label) {
        PdfPCell c = new PdfPCell();
        c.setBackgroundColor(BG_SOFT);
        c.setBorderColor(BORDER);
        c.setPadding(14);
        c.setBorderWidth(0.5f);
        Paragraph v = new Paragraph(value, F_KPI);
        v.setAlignment(Element.ALIGN_CENTER);
        Paragraph l = new Paragraph(label, F_KPI_LBL);
        l.setAlignment(Element.ALIGN_CENTER);
        l.setSpacingBefore(4);
        c.addElement(v);
        c.addElement(l);
        return c;
    }

    private PdfPTable balancesTable(TripBalanceResponse data) {
        PdfPTable t = new PdfPTable(new float[]{60, 40});
        t.setWidthPercentage(100);
        t.addCell(th("Uczestnik", Element.ALIGN_LEFT));
        t.addCell(th("Bilans", Element.ALIGN_RIGHT));

        int row = 0;
        for (ParticipantBalanceDto b : data.balances()) {
            Color bg = (row++ % 2 == 0) ? Color.WHITE : BG_SOFT;
            t.addCell(td(b.userName(), Element.ALIGN_LEFT, bg, F_TD));

            int sign = b.netBalance().signum();
            Color c = sign > 0 ? POSITIVE : sign < 0 ? NEGATIVE : MUTED;
            String prefix = sign > 0 ? "+" : "";
            Font money = font(10, Font.BOLD, c);
            t.addCell(td(prefix + money(b.netBalance()) + " " + data.currencyCode(),
                    Element.ALIGN_RIGHT, bg, money));
        }
        return t;
    }

    private PdfPTable settlementsTable(TripBalanceResponse data) {
        if (data.settlements().isEmpty()) {
            PdfPTable t = new PdfPTable(1);
            t.setWidthPercentage(100);
            PdfPCell c = new PdfPCell(new Phrase(
                    "Wszyscy uczestnicy sa rozliczeni — brak przelewow do wykonania.",
                    font(10, Font.ITALIC, MUTED)));
            c.setPadding(14);
            c.setBackgroundColor(BG_SOFT);
            c.setBorderColor(BORDER);
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            t.addCell(c);
            return t;
        }

        PdfPTable t = new PdfPTable(new float[]{34, 8, 34, 24});
        t.setWidthPercentage(100);
        t.addCell(th("Platnik", Element.ALIGN_LEFT));
        t.addCell(th("", Element.ALIGN_CENTER));
        t.addCell(th("Odbiorca", Element.ALIGN_LEFT));
        t.addCell(th("Kwota", Element.ALIGN_RIGHT));

        int row = 0;
        for (SettlementDto s : data.settlements()) {
            Color bg = (row++ % 2 == 0) ? Color.WHITE : BG_SOFT;
            t.addCell(td(s.fromUserName(), Element.ALIGN_LEFT, bg, F_TD));
            t.addCell(td(">", Element.ALIGN_CENTER, bg, font(10, Font.BOLD, BRAND)));
            t.addCell(td(s.toUserName(), Element.ALIGN_LEFT, bg, F_TD_B));
            t.addCell(td(money(s.amount()) + " " + data.currencyCode(),
                    Element.ALIGN_RIGHT, bg, font(10, Font.BOLD, BRAND_DARK)));
        }
        return t;
    }

    private PdfPCell th(String text, int align) {
        PdfPCell c = new PdfPCell(new Phrase(text, F_TH));
        c.setBackgroundColor(BRAND);
        c.setHorizontalAlignment(align);
        c.setPadding(8);
        c.setBorderColor(BRAND);
        return c;
    }

    private PdfPCell td(String text, int align, Color bg, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setBackgroundColor(bg);
        c.setHorizontalAlignment(align);
        c.setPadding(7);
        c.setBorderColor(BORDER);
        c.setBorderWidth(0.4f);
        return c;
    }

    private static String money(BigDecimal v) {
        if (v == null) return "0.00";
        return v.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    // ---------- nagłówek / stopka na każdej stronie ----------

    private static class HeaderFooter extends PdfPageEventHelper {
        private final TripEvent trip;
        private final String currency;

        HeaderFooter(TripEvent trip, String currency) {
            this.trip = trip;
            this.currency = currency;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document doc) {
            Rectangle ps = doc.getPageSize();

            // Banner
            Rectangle band = new Rectangle(0, ps.getHeight() - 75, ps.getWidth(), ps.getHeight());
            band.setBackgroundColor(BRAND_DARK);
            writer.getDirectContentUnder().rectangle(band);

            String dates = trip.getStartDate() != null && trip.getEndDate() != null
                    ? DAY.format(trip.getStartDate()) + "  -  " + DAY.format(trip.getEndDate())
                    : "";

            com.lowagie.text.pdf.ColumnText.showTextAligned(
                    writer.getDirectContent(), Element.ALIGN_LEFT,
                    new Phrase(safe(trip.getName(), "Wycieczka"), F_TITLE),
                    40, ps.getHeight() - 42, 0);
            com.lowagie.text.pdf.ColumnText.showTextAligned(
                    writer.getDirectContent(), Element.ALIGN_LEFT,
                    new Phrase("Raport rozliczen  •  " + dates + "  •  Waluta: " + currency, F_SUBTITLE),
                    40, ps.getHeight() - 62, 0);

            // Stopka
            String footer = "Wygenerowano: " + TS.format(LocalDateTime.now())
                    + "   |   Strona " + writer.getPageNumber();
            com.lowagie.text.pdf.ColumnText.showTextAligned(
                    writer.getDirectContent(), Element.ALIGN_CENTER,
                    new Phrase(footer, F_FOOTER),
                    ps.getWidth() / 2, 28, 0);
        }

        private static String safe(String s, String fallback) {
            return (s == null || s.isBlank()) ? fallback : s;
        }
    }
}