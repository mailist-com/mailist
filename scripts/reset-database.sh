#!/bin/bash

# Reset database script for Mailist application
# This script drops and recreates the database to allow Liquibase to run migrations from scratch

echo "=========================================="
echo "Mailist Database Reset Script"
echo "=========================================="
echo ""
echo "WARNING: This will delete ALL data from the database!"
echo "Press Ctrl+C to cancel, or Enter to continue..."
read

# Database configuration
DB_NAME="mailist_dev"
DB_USER="postgres"
DB_HOST="localhost"
DB_PORT="5432"

echo ""
echo "Dropping database: $DB_NAME"
psql -U $DB_USER -h $DB_HOST -p $DB_PORT -c "DROP DATABASE IF EXISTS $DB_NAME;"

echo "Creating database: $DB_NAME"
psql -U $DB_USER -h $DB_HOST -p $DB_PORT -c "CREATE DATABASE $DB_NAME;"

echo ""
echo "=========================================="
echo "Database reset complete!"
echo "=========================================="
echo ""
echo "Next steps:"
echo "1. Run the application: ./mvnw spring-boot:run"
echo "2. Liquibase will automatically create all tables"
echo ""
