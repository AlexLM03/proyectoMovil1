"""
Utilidades compartidas para generar los documentos de Acambapan en Word.

Incluye portada, estilos, encabezado/pie con numeracion de pagina, tablas con
formato y un campo de indice (TOC) que Word actualiza al exportar a PDF.
"""
import datetime
import os

from docx import Document
from docx.enum.section import WD_SECTION
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH, WD_BREAK
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Cm, Pt, RGBColor

CAFE = RGBColor(0x8B, 0x5E, 0x3C)
CANELA = RGBColor(0xC9, 0x8B, 0x5E)
TINTA = RGBColor(0x1F, 0x29, 0x37)
GRIS = RGBColor(0x6B, 0x72, 0x80)

EQUIPO = [
    ("Alexander López Mora", "Desarrollo de la arquitectura, capa de datos, "
                              "internacionalización y diseño adaptable"),
    ("Janetzy Maldonado Nava", "Experiencia del cliente: catálogo, carrito, "
                               "cuenta, direcciones y notificaciones"),
    ("Ambar Paola Fujarte Herrera", "Acceso y roles, administración de la "
                                    "panadería, documentación y pruebas"),
]


def campo(parrafo, instruccion):
    """Inserta un campo de Word (TOC, PAGE, NUMPAGES...)."""
    run = parrafo.add_run()
    inicio = OxmlElement("w:fldChar")
    inicio.set(qn("w:fldCharType"), "begin")
    run._r.append(inicio)

    instr = OxmlElement("w:instrText")
    instr.set(qn("xml:space"), "preserve")
    instr.text = instruccion
    run._r.append(instr)

    separar = OxmlElement("w:fldChar")
    separar.set(qn("w:fldCharType"), "separate")
    run._r.append(separar)

    texto = OxmlElement("w:t")
    texto.text = "1"
    run._r.append(texto)

    fin = OxmlElement("w:fldChar")
    fin.set(qn("w:fldCharType"), "end")
    run._r.append(fin)


def sombrear(celda, hex_color):
    shd = OxmlElement("w:shd")
    shd.set(qn("w:val"), "clear")
    shd.set(qn("w:color"), "auto")
    shd.set(qn("w:fill"), hex_color)
    celda._tc.get_or_add_tcPr().append(shd)


def documento_base(titulo, subtitulo):
    doc = Document()

    seccion = doc.sections[0]
    seccion.page_width = Cm(21.0)
    seccion.page_height = Cm(29.7)
    seccion.top_margin = Cm(2.4)
    seccion.bottom_margin = Cm(2.2)
    seccion.left_margin = Cm(2.6)
    seccion.right_margin = Cm(2.4)

    estilos = doc.styles
    normal = estilos["Normal"]
    normal.font.name = "Calibri"
    normal.font.size = Pt(11)
    normal.font.color.rgb = TINTA
    normal.paragraph_format.space_after = Pt(6)
    normal.paragraph_format.line_spacing = 1.15

    for nombre, tam, color, antes, despues in [
        ("Heading 1", 18, CAFE, 18, 8),
        ("Heading 2", 14, CAFE, 14, 6),
        ("Heading 3", 12, CANELA, 10, 4),
    ]:
        st = estilos[nombre]
        st.font.name = "Calibri"
        st.font.size = Pt(tam)
        st.font.color.rgb = color
        st.font.bold = True
        st.paragraph_format.space_before = Pt(antes)
        st.paragraph_format.space_after = Pt(despues)
        st.paragraph_format.keep_with_next = True

    return doc


def portada(doc, titulo, subtitulo, materia, autores_roles, lugar_fecha=None):
    lugar_fecha = lugar_fecha or datetime.date.today().strftime("%d de %B de %Y")

    for _ in range(3):
        doc.add_paragraph()

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run("ACAMBAPAN")
    r.font.size = Pt(40)
    r.font.bold = True
    r.font.color.rgb = CAFE

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run("Panadería y repostería de Acambay")
    r.font.size = Pt(13)
    r.font.color.rgb = CANELA
    r.italic = True

    doc.add_paragraph()
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run(titulo)
    r.font.size = Pt(22)
    r.font.bold = True
    r.font.color.rgb = TINTA

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run(subtitulo)
    r.font.size = Pt(12)
    r.font.color.rgb = GRIS

    for _ in range(2):
        doc.add_paragraph()

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run(materia)
    r.font.size = Pt(12)
    r.font.bold = True
    r.font.color.rgb = CAFE

    doc.add_paragraph()
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run("Integrantes del equipo")
    r.font.size = Pt(11)
    r.font.bold = True
    r.font.color.rgb = GRIS

    for nombre, rol in autores_roles:
        p = doc.add_paragraph()
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = p.add_run(nombre)
        r.font.size = Pt(12)
        r.font.bold = True
        p2 = doc.add_paragraph()
        p2.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r2 = p2.add_run(rol)
        r2.font.size = Pt(9)
        r2.font.color.rgb = GRIS

    for _ in range(2):
        doc.add_paragraph()

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run(lugar_fecha)
    r.font.size = Pt(11)
    r.font.color.rgb = GRIS

    doc.add_page_break()


def encabezado_y_pie(doc, texto_encabezado):
    seccion = doc.sections[0]
    seccion.different_first_page_header_footer = True

    enc = seccion.header.paragraphs[0]
    enc.text = texto_encabezado
    enc.alignment = WD_ALIGN_PARAGRAPH.RIGHT
    for run in enc.runs:
        run.font.size = Pt(8.5)
        run.font.color.rgb = GRIS

    pie = seccion.footer.paragraphs[0]
    pie.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = pie.add_run("Página ")
    r.font.size = Pt(8.5)
    r.font.color.rgb = GRIS
    campo(pie, "PAGE")
    r = pie.add_run(" de ")
    r.font.size = Pt(8.5)
    r.font.color.rgb = GRIS
    campo(pie, "NUMPAGES")


def indice(doc):
    doc.add_heading("Contenido", level=1)
    p = doc.add_paragraph()
    campo(p, 'TOC \\o "1-2" \\h \\z \\u')
    doc.add_page_break()


def parrafo(doc, texto, negrita=False, cursiva=False, tam=11, color=None,
            alineacion=None, espacio_despues=6):
    p = doc.add_paragraph()
    if alineacion:
        p.alignment = alineacion
    r = p.add_run(texto)
    r.bold = negrita
    r.italic = cursiva
    r.font.size = Pt(tam)
    if color:
        r.font.color.rgb = color
    p.paragraph_format.space_after = Pt(espacio_despues)
    return p


def vineta(doc, texto, nivel=0, negrita_hasta=None):
    p = doc.add_paragraph(style="List Bullet" if nivel == 0 else "List Bullet 2")
    if negrita_hasta:
        r = p.add_run(negrita_hasta)
        r.bold = True
        p.add_run(texto)
    else:
        p.add_run(texto)
    p.paragraph_format.space_after = Pt(3)
    return p


def tabla(doc, encabezados, filas, anchos=None, tam=9.5, fuente_encabezado="8B5E3C"):
    t = doc.add_table(rows=1, cols=len(encabezados))
    t.style = "Table Grid"
    t.alignment = WD_TABLE_ALIGNMENT.CENTER

    cab = t.rows[0].cells
    for i, titulo in enumerate(encabezados):
        cab[i].text = ""
        p = cab[i].paragraphs[0]
        r = p.add_run(titulo)
        r.bold = True
        r.font.size = Pt(tam)
        r.font.color.rgb = RGBColor(0xFF, 0xFF, 0xFF)
        sombrear(cab[i], fuente_encabezado)

    for idx, fila in enumerate(filas):
        celdas = t.add_row().cells
        for i, valor in enumerate(fila):
            celdas[i].text = ""
            p = celdas[i].paragraphs[0]
            r = p.add_run(str(valor))
            r.font.size = Pt(tam)
            if idx % 2 == 1:
                sombrear(celdas[i], "FBF3E8")

    if anchos:
        for fila in t.rows:
            for i, ancho in enumerate(anchos):
                fila.cells[i].width = Cm(ancho)

    doc.add_paragraph().paragraph_format.space_after = Pt(2)
    return t


def imagen(doc, ruta, ancho_cm, pie=None):
    if not os.path.exists(ruta):
        parrafo(doc, f"[Falta la imagen {os.path.basename(ruta)}]", cursiva=True,
                color=GRIS)
        return
    doc.add_picture(ruta, width=Cm(ancho_cm))
    doc.paragraphs[-1].alignment = WD_ALIGN_PARAGRAPH.CENTER
    if pie:
        p = doc.add_paragraph()
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = p.add_run(pie)
        r.font.size = Pt(8.5)
        r.italic = True
        r.font.color.rgb = GRIS


def salto(doc):
    doc.add_page_break()


def caja_destacada(doc, titulo, texto, color_hex="FBF3E8"):
    t = doc.add_table(rows=1, cols=1)
    t.style = "Table Grid"
    celda = t.rows[0].cells[0]
    sombrear(celda, color_hex)
    celda.text = ""
    p = celda.paragraphs[0]
    r = p.add_run(titulo)
    r.bold = True
    r.font.size = Pt(10)
    r.font.color.rgb = CAFE
    p2 = celda.add_paragraph()
    r2 = p2.add_run(texto)
    r2.font.size = Pt(9.5)
    doc.add_paragraph().paragraph_format.space_after = Pt(2)
