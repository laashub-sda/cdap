const { ApolloServer } = require('apollo-server');
const { importSchema } = require('graphql-import');
const merge = require('lodash/merge')

const { applicationResolvers } = require('./resolvers/applicationResolvers')
const { namespaceResolvers } = require('./resolvers/namespaceResolvers')
const { metadataResolvers } = require('./resolvers/metadataResolvers')
const { programRecordResolvers } = require('./resolvers/programRecordResolvers')
const { programRecordTypeResolvers } = require('./resolvers/type/programRecordTypeResolver')
const { scheduleResolvers } = require('./resolvers/scheduleResolvers')

const resolvers = merge(applicationResolvers,
                        namespaceResolvers,
                        metadataResolvers,
                        programRecordTypeResolvers,
                        programRecordResolvers,
                        scheduleResolvers)

const typeDefs  = importSchema('schema/rootSchema.graphql')

if(typeof resolvers === 'undefined') {
  throw "The resolvers are undefined"
}

if(typeof typeDefs === 'undefined') {
  throw "The type definitions is undefined"
}

const server = new ApolloServer({ typeDefs, resolvers });

server.listen().then(({ url }) => {
  console.log(`🚀  Server ready at ${url}`);
});