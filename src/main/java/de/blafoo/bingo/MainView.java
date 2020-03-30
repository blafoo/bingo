package de.blafoo.bingo;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H6;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.shared.Registration;

@Route
@Push
@PWA(name = "bingo-ct", shortName = "bingo-ct")
public class MainView extends VerticalLayout implements ComponentEventListener<ClickEvent<Button>> {
	
	private static final long serialVersionUID = -8778411782845391587L;

	private List<BitSet> matrix;
	
	/** Anzahl Spalten */
	private final static int numberOfCols = 4;
	
	/** Anzahl Zeilen */
	private final static int numberOfRows = 4;
	
	/** Name des Spielers */
	private String name = "anonym";
	
	private Registration broadcasterRegistration;

    public MainView() {
    	add(new H2("Bingo"));
		add(new Label("Langeweile bei Besprechungen im täglichen Büroalltag oder zu viel Bullshit? Mit diesem Spiel geht das vorbei. Einfach beim Bingo spielen aufmerksam zuhören. Fällt eines der auf dem Spielfeld gelisteten Worte, kann dieses markiert werden. Eine komplette Reihe horizontal, vertikal oder diagonal? Bingo!"));
		add(new H6(""));
		
        // Name des Spielers
		final TextField spieler = new TextField();
		spieler.setPlaceholder(name);
		spieler.setLabel("Name");
		spieler.addValueChangeListener(e -> {
			name = e.getValue();
		});
		add(spieler);
        
        // Bingo-Tableau
		createBingoGrid(this);
    }
    
    /**
     * Bingo-Tableau erzeugen
     * @param parent
     */
    private void createBingoGrid(VerticalLayout parent) {

		List<String> data = BingoModel.getData("Besprechungen");
		Collections.shuffle(data); 
		
		HorizontalLayout layout = new HorizontalLayout();
		matrix = new ArrayList<>();
		
		for ( int row = 0; row < numberOfRows; row++) {
			matrix.add(new BitSet(numberOfCols));
		}
		
		for ( int col = 0; col < numberOfCols; col++) {
			createBingoCol(layout, col, data.subList(numberOfRows * col, numberOfRows * (col+1)));
		}
		
		parent.add(layout);
	}

    /**
     * Eine "Spalte" erzeugen
     * @param parent
     * @param col
     * @param colData
     */
	private void createBingoCol(HorizontalLayout parent, int col, List<String> colData) {
		
		VerticalLayout layout = new VerticalLayout();
		// Abstände um die Buttons minimieren
		layout.setPadding(false);
		layout.setMargin(false);
		layout.setSpacing(false);
		layout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);
		layout.setWidth("25%");
		
		int row = 0;
		for ( String content : colData) {
			BingoButton button = new BingoButton(col, row, content, this);
			// per Default haben Buttons nur die minimal notwendige Größe
			button.setHeight("75px");
			button.setWidth("175px");
			layout.add(button);
			row++;
		}

		parent.add(layout);
	}
	
	/**
	 * Callback vom BingoButton. Wird beim Click auf ein Feld ausgelöst.
	 */
	@Override
	public void onComponentEvent(ClickEvent<Button> event) {
		BingoButton button = (BingoButton) event.getSource();
		if ( button.isChecked()) {
			matrix.get(button.getRow()).set(button.getCol());
		} else {
			matrix.get(button.getRow()).clear(button.getCol());
		}

		checkBingo();
	}
	
	/** 
	 * Gibt es eine 'komplette' Reihe?
	 */
	private void checkBingo() {

		// horizontal
		for (BitSet col : matrix) {
			if ( col.cardinality() == numberOfCols) {
				bingo();
			}
		}
		// vertikal
		for ( int col = 0; col < numberOfCols; col++) {
			boolean isRowComplete = true;
			for ( int row = 0; row < numberOfRows; row++) {
				if ( !matrix.get(row).get(col) ) {
					isRowComplete = false;
					break;
				}
			}
			if ( isRowComplete) {
				bingo();
			}
		}
		
		// vertikal LO->RU
		boolean isComplete = true;
		for ( int col = 0; col < numberOfCols; col++) {
			if ( !matrix.get(col).get(col) ) {
				isComplete = false;
				break;
			}
		}
		if ( isComplete) {
			bingo();
		}

		// vertikal LU->RO
		isComplete = true;
		for ( int col = 0; col < numberOfCols; col++) {
			if ( !matrix.get(numberOfRows-col-1).get(col) ) {
				isComplete = false;
				break;
			}
		}
		if ( isComplete) {
			bingo();
		}

	}
	
	/**
	 * Eine Reihe ist komplett. Wir können in Jubel ausbrechen!
	 */
	private void bingo() {
		Dialog bingo = new Dialog();
		H1 h1 = new H1("BINGO!");
		h1.getStyle().set("text-align", "center");

		Image image = new Image("/images/bingo.png", "Bingo!");
		
		AudioPlayer player = new AudioPlayer("/images/bingo.m4a", true);
		
		bingo.add(h1, image, player);
		bingo.open();
		
		Broadcaster.broadcast(name);
	}
	
	@Override
	protected void onAttach(AttachEvent attachEvent) {
		UI ui = attachEvent.getUI();
		broadcasterRegistration = Broadcaster.register(newMessage -> {
			ui.access(() -> showNotification("BINGO! '"+name+"' hat gewonnen!", NotificationVariant.LUMO_SUCCESS));
		});
	}

	@Override
	protected void onDetach(DetachEvent detachEvent) {
		broadcasterRegistration.remove();
		broadcasterRegistration = null;
	}
	
	private void showNotification(String text, NotificationVariant variant) {
		Notification notification = new Notification(text);
		if ( variant != null ) {
			notification.addThemeVariants(variant);
		}
		notification.setDuration(5000);
		notification.open();
	}


}
