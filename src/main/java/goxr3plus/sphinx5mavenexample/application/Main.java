package main.java.goxr3plus.sphinx5mavenexample.application;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Port;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.result.WordResult;

public class Main {
	private String screen="";
	private LiveSpeechRecognizer recognizer;
	private Logger logger = Logger.getLogger(getClass().getName());
	private String speechRecognitionResult;
	private boolean ignoreSpeechRecognitionResults = false;
	private boolean speechRecognizerThreadRunning = false;
	private boolean resourcesThreadRunning;
	private ExecutorService eventsExecutorService = Executors.newFixedThreadPool(2);
	public Main() {
		/**
		 * Se importó la librería logger para enviar mensajes por consola sobre el estado del reconocedor.
		 **/
		logger.log(Level.INFO, "Loading Speech Recognizer...\n");
		/**
		 * Se establecio la configuracion del modelo, indicando la ruta de la libreria del modelo
		 * la ruta del diccionario
		 * y la ruta del modelo entrenado.
		 **/
		Configuration configuration = new Configuration();
		configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
		configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
		configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");
		/**
		 * Para aumentar la precisión sobre un conjunto de posibles resultados
		 * se creó un diccionario con las palabras esperadas.
		 **/
		configuration.setGrammarPath("resource:/grammars");
		configuration.setGrammarName("grammar");
		configuration.setUseGrammar(true);

		/**
		 * se iniciara el reconocedor de voz cuando se detecte un microfono en el equipo
		 * y se aplicara la configuracion previamente establecida
		 **/
		try {
			recognizer = new LiveSpeechRecognizer(configuration);
		} catch (IOException ex) {
			logger.log(Level.SEVERE, null, ex);
		}
		/**
		 * se iniciara el hilo del proceso de reconocimiento de voz
		 **/
		startResourcesThread();
		startSpeechRecognition();
	}

	public synchronized void startSpeechRecognition() {
		/**
		 * emision del mensaje que el reconocedor inicio correctamente
		 **/
		if (speechRecognizerThreadRunning)
			logger.log(Level.INFO, "Speech Recognition Thread already running...\n");
		else
			eventsExecutorService.submit(() -> {
				/**
				 * se indica que se compruebe que el reconocedor este ejecutandose
				 * y que los mensajes de error y resultados al terminar no sean ignorados
				 **/
				speechRecognizerThreadRunning = true;
				ignoreSpeechRecognitionResults = false;

				/**
				 * se inicia el servicio de reconocimiento
				 **/
				recognizer.startRecognition(true);
				/**
				 * se emite el mensaje de que el servicio esta a la escucha
				 **/
				logger.log(Level.INFO, "You can start to speak...\n");
				
				try {
					while (speechRecognizerThreadRunning) {
						/**
						 * se predice la voz recibida
						 **/
						SpeechResult speechResult = recognizer.getResult();
						/**
						 * si se detecta algun problema se envia el mensaje problema corespondiente
						 **/
						if (!ignoreSpeechRecognitionResults) {

							if (speechResult == null)
								logger.log(Level.INFO, "I can't understand what you said.\n");
							else {
								/**
								 * en caso contrario, se envia un mensaje que regresa la palabra reconocida
								 **/
								speechRecognitionResult = speechResult.getHypothesis();

								System.out.println("You said: [" + speechRecognitionResult + "]\n");
								/**
								 * se calcula los numeros que envia el usuario por voz
								 **/

								calc(speechRecognitionResult);

							}
						} else
							logger.log(Level.INFO, "Ingoring Speech Recognition Results...");
						
					}
				} catch (Exception ex) {
					logger.log(Level.WARNING, null, ex);
					speechRecognizerThreadRunning = false;
				}
				
				logger.log(Level.INFO, "SpeechThread has exited...");
				
			});
	}
	

	public synchronized void stopIgnoreSpeechRecognitionResults() {
		ignoreSpeechRecognitionResults = false;
	}

	public synchronized void ignoreSpeechRecognitionResults() {
		ignoreSpeechRecognitionResults = true;
		
	}

	public void startResourcesThread() {
		if (resourcesThreadRunning)
			logger.log(Level.INFO, "Resources Thread already running...\n");
		else
			eventsExecutorService.submit(() -> {
				try {
					resourcesThreadRunning = true;
					/**
					 * se detecta si existe un microfono
					 **/
					while (true) {

						/**
						 * si no esta disponible, se envia el mensaje
						 **/
						if (!AudioSystem.isLineSupported(Port.Info.MICROPHONE))
							logger.log(Level.INFO, "Microphone is not available.\n");

						/**
						 * se inicia una pausa de 350 milisegundos antes de volver a predecir
						 **/
						Thread.sleep(350);
					}
					
				} catch (InterruptedException ex) {
					logger.log(Level.WARNING, null, ex);
					resourcesThreadRunning = false;
				}
			});
	}
	/**
	 * metodo que retorna lo que el usuario dijo
	 **/
	public void makeDecision(String speech , List<WordResult> speechWords) {
		System.out.println(speech);
	}

	public void calc(String speech){
		/**
		 * se convierte las palabras de interes en acciones, simbolos o numeros correspondientes.
		 **/
		switch (speech){
			case "clear":screen="";
				break;
			case "remove":screen=screen.substring(0,screen.length()-1);
				break;
			case "result":screen=ExcecuteOperation(screen);
				break;
			case "plus":screen+=" + ";
				break;
			case "minus":screen+=" - ";
				break;
			case "multiply":screen+=" * ";
				break;
			case "divide":screen+=" / ";
				break;
			case "one":screen+="1";
				break;
			case "two":screen+="2";
				break;
			case "three":screen+="3";
				break;
			case "four":screen+="4";
				break;
			case "five":screen+="5";
				break;
			case "six":screen="6";
				break;
			case "seven":screen+="7";
				break;
			case "eight":screen+="8";
				break;
			case "nine":screen+="9";
				break;
			case "zero":screen+="0";
				break;
		}
		System.out.println(screen);
	}


	public String ExcecuteOperation(String screen){
		/**
		 * se obtiene los simbolos del mensaje en pantalla
		 **/
		String[] screenData=getOperationParts(screen);
		String result="";
		int value=0;
		/**
		 * se inicia el operador sin simbolo.
		 **/
	String currentOperation="";
		/**
		 *se recorre todos los simbolos y numeros encontrados en el mensaje
		 **/
		for(int i=0; i<screenData.length; i++ ){
			/**
			 * si se tiene un numero y antes no se detecto un simbolo, se guarda
			 **/
			if(validNumber(screenData[i])){
				if(currentOperation.length()==0){
					value=Integer.parseInt(screenData[i]);
				}
				/**
				 * si se indico una suma,resta,multiplicacion o division, anteriormente
				 * entonces de realizara la operacion con los dos numeros mas recientes.
				 **/
				else{
					switch (currentOperation){
							case "+": value=value+Integer.parseInt(screenData[i]);
								break;
							case "-": value=value-Integer.parseInt(screenData[i]);
								break;
							case "*": value=value*Integer.parseInt(screenData[i]);
								break;
							case "/": value=value / Integer.parseInt(screenData[i]);
								break;
						}
				}
			}
			else{
				/**
				 * si se introdujo solamente un simbolo, se guarda
				 **/
				currentOperation=screenData[i];
			}
		}
		/**
		 * se pasa el resultado a una cadena para ser enviada a consola
		 **/
		result=String.valueOf(value);
		return result;
	}
	/**
	 * metodo para verificar si una cadena es un numero
	 **/
	private boolean validNumber(String screenDatum) {
		return (screenDatum != null && screenDatum.matches("[0-9]+"));
	}


	public String[] getOperationParts(String string){
		String[] result=new String[0];
		if (screen != null) {
			result = string.split(" ");
		}
	return result;
	}


	public boolean getIgnoreSpeechRecognitionResults() {
		return ignoreSpeechRecognitionResults;
	}
	
	public boolean getSpeechRecognizerThreadRunning() {
		return speechRecognizerThreadRunning;
	}

	public static void main(String[] args) {
		new Main();
	}
}
