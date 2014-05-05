package com.worldline.awltech.i18ntools.editor.data.model;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import com.worldline.awltech.i18ntools.editor.LogMessage;

/**
 * Resource Bundle model object. Contains all messages information and file
 * location
 * 
 * @author mvanbesien
 * 
 */
public class I18NResourceBundle {

	private String name;

	private ICompilationUnit enumeration;

	private Map<Locale, IFile> locales;

	private List<I18NEntry> defaultMessages = new ArrayList<>();

	private List<I18NEntry> newMessages = new ArrayList<>();

	private boolean dirty = false;

	public static class I18NResourceBundleBuilder {

		private String name;

		private ICompilationUnit enumeration;

		private List<String> literals = new ArrayList<>();

		private Map<Locale, IFile> locales = new LinkedHashMap<>();

		public I18NResourceBundleBuilder withName(String name) {
			this.name = name;
			return this;
		}

		public I18NResourceBundleBuilder withEnumeration(ICompilationUnit enumeration) {
			this.enumeration = enumeration;
			return this;
		}

		public I18NResourceBundleBuilder withLiterals(List<String> literals) {
			this.literals = literals;
			return this;
		}

		public I18NResourceBundleBuilder withLocales(Map<Locale, IFile> locales) {
			this.locales.clear();
			this.locales.putAll(locales);
			return this;
		}

		public I18NResourceBundle create() {
			if (name == null || enumeration == null) {
				return null;
			}

			Map<Locale, Properties> propertiesMap = new HashMap<>();
			for (Locale locale : this.locales.keySet()) {
				IFile iFile = this.locales.get(locale);
				if (iFile != null && iFile.exists()) {
					Properties properties = new Properties();
					try {
						iFile.refreshLocal(IResource.DEPTH_ZERO, null);
						properties.load(iFile.getContents());
					} catch (IOException | CoreException e) {
						LogMessage.warn().message("An error occurred while refreshing file.").throwable(e).log();
					}
					propertiesMap.put(locale, properties);
				}
			}

			I18NResourceBundle resourceBundle = new I18NResourceBundle();
			resourceBundle.name = name;
			resourceBundle.enumeration = enumeration;
			resourceBundle.locales = locales;
			for (String literal : literals) {
				I18NEntry message = new I18NEntry(literal);
				resourceBundle.defaultMessages.add(message);
				for (Entry<Locale, Properties> entry : propertiesMap.entrySet()) {
					Properties properties = entry.getValue();
					String value = properties.containsKey(literal) ? properties.getProperty(literal) : null;
					message.withMessage(entry.getKey(), new I18NMessage(value, I18NMessageStatus.BUILT));
				}
			}
			return resourceBundle;
		}
	}

	private I18NResourceBundle() {
	}

	public String getName() {
		return name;
	}

	public List<I18NEntry> getAllMessages() {
		List<I18NEntry> allMessages = new ArrayList<>();
		allMessages.addAll(this.defaultMessages);
		allMessages.addAll(this.newMessages);
		return allMessages;
	}

	public ICompilationUnit getEnumeration() {
		return enumeration;
	}

	public Map<Locale, IFile> getLocales() {
		return locales;
	}

	public void addLocale(Locale locale) {
		if (this.locales.containsKey(locale))
			return;

		IContainer bundlesParent = this.locales.get(null).getParent();
		String newFileName = this.name + "_" + locale.getLanguage() + "_" + locale.getCountry() + ".properties";
		IFile newFile = bundlesParent.getFile(new Path(newFileName));
		this.locales.put(locale, newFile);
		this.dirty = true;
	}

	public boolean isDirty() {
		if (this.dirty)
			return true;
		else {
			for (I18NEntry message : getAllMessages()) {
				if (message.getDefaultMessage() == null
						|| message.getDefaultMessage().getStatus() != I18NMessageStatus.BUILT)
					return true;
				for (I18NMessage localizedMessage : message.getLocalizedMessages().values())
					if (localizedMessage == null || localizedMessage.getStatus() != I18NMessageStatus.BUILT)
						return true;
			}
		}
		return false;
	}

	public void save() {
		boolean dirty = this.addNewLiterals();
		try {
			Map<Locale, Properties> mappedProperties = new HashMap<>();
			for (Locale locale : this.locales.keySet()) {
				Properties properties = new Properties();
				mappedProperties.put(locale, properties);
				IFile file = this.locales.get(locale);
				if (file.exists()) {
					try {
						file.refreshLocal(IResource.DEPTH_ZERO, null);
						properties.load(file.getContents());
					} catch (IOException | CoreException e) {
						LogMessage.warn().message("An error occurred while refreshing file.").throwable(e).log();
					}
				}
			}

			List<Locale> modifiedLocales = new ArrayList<>();
			for (I18NEntry entry : this.getAllMessages()) {
				if (entry.getDefaultMessage().getStatus() != I18NMessageStatus.BUILT) {
					Properties properties = mappedProperties.get(null);
					properties.setProperty(entry.getName(), entry.getDefaultMessage().getValue());
					if (!modifiedLocales.contains(null))
						modifiedLocales.add(null);
				}
				for (Locale locale : entry.getLocalizedMessages().keySet()) {
					I18NMessage i18nMessage = entry.getLocalizedMessages().get(locale);
					if (i18nMessage.getStatus() != I18NMessageStatus.BUILT) {
						Properties properties = mappedProperties.get(locale);
						properties.setProperty(entry.getName(), entry.getLocalizedMessages().get(locale).getValue());
						if (!modifiedLocales.contains(locale))
							modifiedLocales.add(locale);
					}
				}
			}

			for (Locale locale : modifiedLocales) {
				IFile file = this.locales.get(locale);
				try {
					FileWriter writer = new FileWriter(file.getLocation().toFile());
					mappedProperties.get(locale).store(writer, null);
					dirty = true;
				} catch (IOException e) {
					LogMessage.error().message("An error occurred while saving file.").throwable(e).log();
				}
			}

		} finally {
			try {
				for (I18NEntry entry : getAllMessages()) {
					entry.getDefaultMessage().resetStatus();
					for (I18NMessage localMessage : entry.getLocalizedMessages().values())
						localMessage.resetStatus();
				}
				if (dirty) {
					enumeration.getJavaProject().getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
				}
			} catch (CoreException e) {
				LogMessage.warn().message("An error occurred while refreshing file.").throwable(e).log();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private boolean addNewLiterals() {
		if (this.newMessages.size() == 0)
			return false;

		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(enumeration);
		CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
		compilationUnit.recordModifications();

		EnumDeclaration enumDeclaration = null;
		for (Iterator<?> iterator = compilationUnit.types().iterator(); iterator.hasNext() && enumDeclaration == null;) {
			Object next = iterator.next();
			if (next instanceof EnumDeclaration)
				enumDeclaration = (EnumDeclaration) next;
		}

		if (enumDeclaration == null)
			return false;

		// Check if entry exists !!!

		AST ast = enumDeclaration.getAST();
		for (I18NEntry entry : newMessages) {
			String entryName = entry.getName();
			boolean nameExists = false;
			for (Object o : enumDeclaration.enumConstants()) {
				EnumConstantDeclaration ecd = (EnumConstantDeclaration) o;
				if (entryName.equals(ecd.getName().getFullyQualifiedName()))
					nameExists = true;
			}
			if (!nameExists) {
				EnumConstantDeclaration newEnumConstantDeclaration = enumDeclaration.getAST()
						.newEnumConstantDeclaration();
				newEnumConstantDeclaration.setName(ast.newSimpleName(entryName));

				StringLiteral literal = ast.newStringLiteral();
				literal.setLiteralValue(entryName);
				newEnumConstantDeclaration.arguments().add(literal);
				enumDeclaration.enumConstants().add(newEnumConstantDeclaration);
			}
		}

		try {
			IDocument document = new Document(enumeration.getSource());
			final TextEdit rewrite = compilationUnit.rewrite(document, null);
			rewrite.apply(document);
			final String newSource = document.get();
			enumeration.getBuffer().setContents(newSource);
			enumeration.getResource().refreshLocal(IResource.DEPTH_ZERO, null);
			enumeration.save(null, false);

			defaultMessages.addAll(newMessages);
			newMessages.clear();

		} catch (MalformedTreeException | BadLocationException | CoreException e) {
			LogMessage.error().message("An error occurred while saving file.").throwable(e).log();
		}

		return false;
	}

	public void addLiteral(String literal) {
		for (I18NEntry message : this.defaultMessages) {
			if (literal.equals(message.getName()))
				return;
		}
		for (I18NEntry message : this.newMessages) {
			if (literal.equals(message.getName()))
				return;
		}
		I18NEntry i18nEntry = new I18NEntry(literal);
		i18nEntry.updateDefaultMessage("");
		this.newMessages.add(i18nEntry);
	}

}
