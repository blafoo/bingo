package de.blafoo.bingo;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;

/**
 * Wrapper für das HTML5 'audio' Element. Entspricht <audio id="audioControl" autoplay="true" src="..." type="audio/wav"></audio>
 */
@Tag("audio")
public class AudioPlayer extends Component {

    private static final long serialVersionUID = -7070434811376007422L;
    
    private static final String ID = "audioControl";

	public AudioPlayer(String sound, boolean autoPlay) {
		getElement().setAttribute("id", ID);
		setSource(sound);
        getElement().setAttribute("autoplay",autoPlay);
    }

    public void setSource(String path) {
        getElement().setAttribute("src",path);
    }
  
}
