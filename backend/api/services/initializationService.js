const connection = require('../../db/connection');

/**
 * Service to handle initialization tasks when the server starts
 */
class InitializationService {
  /**
   * Set all plants' age to 1 to use the default model
   */
  async resetAllPlantsToDefaultModel() {
    try {
      console.log('Initializing all plants to use the default model (age = 1)...');
      
      const [result] = await connection.query(`
        UPDATE Plants 
        SET age = 1 
        WHERE 1=1
      `);
      
      console.log(`Successfully reset ${result.affectedRows} plants to use the default model`);
      return result.affectedRows;
    } catch (error) {
      console.error('Error resetting plants to default model:', error);
      throw error;
    }
  }

  /**
   * Initialize all required services on server startup
   */
  async initialize() {
    try {
      console.log('Starting initialization service...');
      const updatedPlants = await this.resetAllPlantsToDefaultModel();
      console.log(`Initialization service completed successfully - Reset ${updatedPlants} plants to default model`);
    } catch (error) {
      console.error('Error during initialization:', error);
    }
  }
}

module.exports = new InitializationService(); 