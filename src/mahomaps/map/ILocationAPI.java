package mahomaps.map;

interface ILocationAPI {

	void setThread(GeoUpdateThread geoUpdateThread);

	void setupListener();

	double[] getLastKnownCoordinates();

	void resetProvider() throws Exception;
}