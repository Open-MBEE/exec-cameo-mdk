# Configuration file for the Sphinx documentation builder.

# -- Project information

project = 'OpenMBEE Model Development Kit'
copyright = '2022, OpenMBEE Open Source Community'
author = 'OpenMBEE Open Source Community'

release = '0.1'
version = '5.1.0'

# -- General configuration

extensions = [
    'sphinx.ext.duration',
    'sphinx.ext.doctest',
    'sphinx.ext.autodoc',
    'sphinx.ext.autosectionlabel',
    'sphinx.ext.autosummary',
    'sphinx.ext.intersphinx',
]

intersphinx_mapping = {
    'python': ('https://docs.python.org/3/', None),
    'sphinx': ('https://www.sphinx-doc.org/en/master/', None),
}
intersphinx_disabled_domains = ['std']

templates_path = ['_templates']

# -- Options for HTML output
# OpenMBEE brand guide: https://www.openmbee.org/brand.html
html_static_path = ['_static']
html_logo = '_static/openmbee-logo.svg'
html_favicon = '_static/favicon.svg'
html_theme_options = {
    'font_family': "'ABeeZee', sans-serif",
    'head_font_family': "'ABeeZee', sans-serif",
    'caption_font_family': "'ABeeZee', sans-serif",
    'body_text': '#10052F',
    'link': '#10052F',
    'link_hover': '#10052F',
    'sidebar_header': '#10052F',
    'sidebar_link': '#10052F',
    'sidebar_text': '#10052F',
    'sidebar_hr': '#D6D6D6',
    'sidebar_search_button': '#D6D6D6',
    'footer_text': '#10052F',
    'gray_1': '#10052F',
    'gray_2': '#D6D6D6',
    'gray_3': '#D6D6D6',
}
numfig = True
autosectionlabel_prefix_document = True

# -- Options for EPUB output
epub_show_urls = 'footnote'
